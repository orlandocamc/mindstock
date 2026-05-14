// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/service/ItemService.java
//
// CONCEPTO POO: Patrón Service
// Encapsula TODA la lógica de negocio. El Controller nunca
// accede al Repository directamente.
//
// CONCEPTO POO: Inyección de Dependencias
// Spring inyecta el Repository automáticamente via constructor.
// Esto es Inversión de Control (IoC).
// ============================================================
package com.ibero.mindstock.service;

import com.ibero.mindstock.dto.ItemDTO;
import com.ibero.mindstock.dto.StatsDTO;
import com.ibero.mindstock.model.Item;
import com.ibero.mindstock.model.enums.ItemStatus;
import com.ibero.mindstock.model.enums.RequestStatus;
import com.ibero.mindstock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  // Inyección de dependencias por constructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final AlertRepository alertRepository;

    /**
     * Obtiene todos los items y los convierte a DTOs
     */
    public List<ItemDTO> findAll() {
        return itemRepository.findAll().stream()
                .map(this::toDTO)   // CONCEPTO POO: Method reference
                .collect(Collectors.toList());
    }

    /**
     * Busca un item por ID
     */
    public ItemDTO findById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item no encontrado con ID: " + id));
        return toDTO(item);
    }

    /**
     * Filtra items por categoría
     */
    public List<ItemDTO> findByCategory(Long categoryId) {
        return itemRepository.findByCategoryId(categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtra items por laboratorio
     */
    public List<ItemDTO> findByLab(Long labId) {
        return itemRepository.findByLabId(labId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca items por nombre (búsqueda parcial)
     */
    public List<ItemDTO> search(String query) {
        return itemRepository.findByNombreContainingIgnoreCase(query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Solo items disponibles
     */
    public List<ItemDTO> findDisponibles() {
        return itemRepository.findAllDisponibles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtra items por status
     */
    public List<ItemDTO> findByStatus(String status) {
        ItemStatus itemStatus = ItemStatus.valueOf(status.toUpperCase());
        return itemRepository.findByStatus(itemStatus).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Estadísticas generales del dashboard
     */
    public StatsDTO getStats() {
        List<Item> allItems = itemRepository.findAll();

        long disponibles = allItems.stream()
                .filter(i -> i.getStatus() == ItemStatus.DISPONIBLE)
                .count();

        long prestados = allItems.stream()
                .filter(i -> i.getCantidadTotal() - i.getCantidadDisponible() > 0)
                .count();

        long solicitudesPendientes = loanRequestRepository
                .findByStatus(RequestStatus.PENDIENTE).size();

        return StatsDTO.builder()
                .totalItems((long) allItems.size())
                .itemsDisponibles(disponibles)
                .itemsPrestados(prestados)
                .solicitudesPendientes(solicitudesPendientes)
                .alertasNoLeidas(0L) // Se actualizará cuando implementemos alertas
                .build();
    }

    // -------------------------------------------------------
    // CONCEPTO POO: Método privado de conversión
    // Encapsula la transformación Entity → DTO
    // -------------------------------------------------------
    private ItemDTO toDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .nombre(item.getNombre())
                .descripcion(item.getDescripcion())
                .categoryNombre(item.getCategory().getNombre())
                .categoryId(item.getCategory().getId())
                .labNombre(item.getLab().getNombre())
                .labId(item.getLab().getId())
                .cantidadTotal(item.getCantidadTotal())
                .cantidadDisponible(item.getCantidadDisponible())
                .status(item.getStatus().name())
                .imagenUrl(item.getImagenUrl())
                .build();
    }
}