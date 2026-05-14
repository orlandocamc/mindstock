// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/service/LoanService.java
//
// CONCEPTO POO: Orquestación de objetos
// Este servicio coordina múltiples entidades: User, Item,
// LoanRequest, Loan. Cada objeto mantiene su propia lógica
// interna y el Service los conecta.
// ============================================================
package com.ibero.mindstock.service;

import com.ibero.mindstock.dto.CreateLoanRequestDTO;
import com.ibero.mindstock.dto.LoanDTO;
import com.ibero.mindstock.dto.LoanRequestDTO;
import com.ibero.mindstock.model.*;
import com.ibero.mindstock.model.enums.*;
import com.ibero.mindstock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRequestRepository loanRequestRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Crea una nueva solicitud de préstamo
     * (se llama cuando el alumno confirma en la interfaz)
     */
    @Transactional
    public LoanRequestDTO createRequest(CreateLoanRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.estaDisponible()) {
            throw new RuntimeException("El item '" + item.getNombre() + "' no está disponible");
        }

        if (dto.getCantidad() > item.getCantidadDisponible()) {
            throw new RuntimeException("Solo hay " + item.getCantidadDisponible() + " unidades disponibles");
        }

        LoanRequest request = LoanRequest.builder()
                .user(user)
                .item(item)
                .cantidad(dto.getCantidad())
                .detectedBy(DetectionMethod.valueOf(dto.getDetectedBy()))
                .status(RequestStatus.PENDIENTE)
                .confidenceScore(dto.getConfidenceScore())
                .build();

        LoanRequest saved = loanRequestRepository.save(request);
        return toRequestDTO(saved);
    }

    /**
     * El laboratorista aprueba una solicitud → se crea el préstamo
     */
    @Transactional
    public LoanDTO approveRequest(Long requestId, Long laboratoristaId) {
        LoanRequest request = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!request.estaPendiente()) {
            throw new RuntimeException("Esta solicitud ya fue procesada");
        }

        User laboratorista = userRepository.findById(laboratoristaId)
                .orElseThrow(() -> new RuntimeException("Laboratorista no encontrado"));

        if (!laboratorista.esLaboratorista()) {
            throw new RuntimeException("El usuario no tiene permisos de laboratorista");
        }

        // 1. Aprobar la solicitud
        request.aprobar();
        loanRequestRepository.save(request);

        // 2. Retirar del inventario (el Item maneja su propia lógica)
        Item item = request.getItem();
        boolean exito = item.retirar(request.getCantidad());
        if (!exito) {
            throw new RuntimeException("No hay suficientes unidades disponibles");
        }
        itemRepository.save(item);

        // 3. Crear el préstamo
        Loan loan = Loan.builder()
                .request(request)
                .approvedBy(laboratorista)
                .fechaPrestamo(LocalDateTime.now())
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(7))
                .status(LoanStatus.ACTIVO)
                .build();

        Loan saved = loanRepository.save(loan);
        return toLoanDTO(saved);
    }

    /**
     * Rechazar una solicitud
     */
    @Transactional
    public LoanRequestDTO rejectRequest(Long requestId) {
        LoanRequest request = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        request.rechazar();
        LoanRequest saved = loanRequestRepository.save(request);
        return toRequestDTO(saved);
    }

    /**
     * Registrar devolución de un préstamo
     */
    @Transactional
    public LoanDTO returnLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!loan.estaActivo() && !loan.estaVencido()) {
            throw new RuntimeException("Este préstamo ya fue devuelto");
        }

        // El objeto Loan maneja su propia lógica de devolución
        loan.registrarDevolucion();

        itemRepository.save(loan.getRequest().getItem());
        Loan saved = loanRepository.save(loan);
        return toLoanDTO(saved);
    }

    /**
     * Obtener todas las solicitudes pendientes
     */
    public List<LoanRequestDTO> findPendingRequests() {
        return loanRequestRepository.findByStatus(RequestStatus.PENDIENTE).stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener préstamos activos
     */
    public List<LoanDTO> findActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVO).stream()
                .map(this::toLoanDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener préstamos de un usuario
     */
    public List<LoanDTO> findByUser(Long userId) {
        return loanRepository.findByRequestUserId(userId).stream()
                .map(this::toLoanDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las solicitudes
     */
    public List<LoanRequestDTO> findAllRequests() {
        return loanRequestRepository.findAll().stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Conversores Entity → DTO
    // -------------------------------------------------------
    private LoanRequestDTO toRequestDTO(LoanRequest r) {
        return LoanRequestDTO.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getNombreCompleto())
                .itemId(r.getItem().getId())
                .itemName(r.getItem().getNombre())
                .cantidad(r.getCantidad())
                .detectedBy(r.getDetectedBy().name())
                .status(r.getStatus().name())
                .confidenceScore(r.getConfidenceScore())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().format(FORMATTER) : null)
                .build();
    }

    private LoanDTO toLoanDTO(Loan l) {
        return LoanDTO.builder()
                .id(l.getId())
                .requestId(l.getRequest().getId())
                .userName(l.getRequest().getUser().getNombreCompleto())
                .itemName(l.getRequest().getItem().getNombre())
                .cantidad(l.getRequest().getCantidad())
                .approvedByName(l.getApprovedBy().getNombreCompleto())
                .fechaPrestamo(l.getFechaPrestamo().format(FORMATTER))
                .fechaDevolucionEsperada(l.getFechaDevolucionEsperada().format(FORMATTER))
                .fechaDevolucionReal(l.getFechaDevolucionReal() != null
                        ? l.getFechaDevolucionReal().format(FORMATTER) : null)
                .status(l.getStatus().name())
                .build();
    }
}