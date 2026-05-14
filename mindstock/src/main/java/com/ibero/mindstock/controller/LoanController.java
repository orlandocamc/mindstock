// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/controller/LoanController.java
//
// El controller más complejo: gestiona solicitudes y préstamos
// ============================================================
package com.ibero.mindstock.controller;

import com.ibero.mindstock.dto.CreateLoanRequestDTO;
import com.ibero.mindstock.dto.LoanDTO;
import com.ibero.mindstock.dto.LoanRequestDTO;
import com.ibero.mindstock.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    // -------------------------------------------------------
    // SOLICITUDES DE PRÉSTAMO
    // -------------------------------------------------------

    // POST /api/loans/requests → Crear solicitud
    @PostMapping("/requests")
    public ResponseEntity<LoanRequestDTO> createRequest(@RequestBody CreateLoanRequestDTO dto) {
        LoanRequestDTO created = loanService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/loans/requests → Todas las solicitudes
    @GetMapping("/requests")
    public ResponseEntity<List<LoanRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(loanService.findAllRequests());
    }

    // GET /api/loans/requests/pending → Solicitudes pendientes
    @GetMapping("/requests/pending")
    public ResponseEntity<List<LoanRequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(loanService.findPendingRequests());
    }

    // PUT /api/loans/requests/1/approve?laboratoristaId=1 → Aprobar
    @PutMapping("/requests/{requestId}/approve")
    public ResponseEntity<LoanDTO> approveRequest(
            @PathVariable Long requestId,
            @RequestParam Long laboratoristaId) {
        return ResponseEntity.ok(loanService.approveRequest(requestId, laboratoristaId));
    }

    // PUT /api/loans/requests/1/reject → Rechazar
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<LoanRequestDTO> rejectRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(loanService.rejectRequest(requestId));
    }

    // -------------------------------------------------------
    // PRÉSTAMOS ACTIVOS
    // -------------------------------------------------------

    // GET /api/loans/active → Préstamos activos
    @GetMapping("/active")
    public ResponseEntity<List<LoanDTO>> getActiveLoans() {
        return ResponseEntity.ok(loanService.findActiveLoans());
    }

    // GET /api/loans/user/1 → Préstamos de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.findByUser(userId));
    }

    // PUT /api/loans/1/return → Registrar devolución
    @PutMapping("/{loanId}/return")
    public ResponseEntity<LoanDTO> returnLoan(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.returnLoan(loanId));
    }
}