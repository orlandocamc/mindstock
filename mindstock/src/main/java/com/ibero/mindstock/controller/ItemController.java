// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/controller/ItemController.java
//
// CONCEPTO POO: Separación de responsabilidades
// El Controller SOLO maneja HTTP. No tiene lógica de negocio.
// Delega todo al Service.
// ============================================================
package com.ibero.mindstock.controller;

import com.ibero.mindstock.dto.ItemDTO;
import com.ibero.mindstock.dto.StatsDTO;
import com.ibero.mindstock.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // Permite que React se conecte
public class ItemController {

    private final ItemService itemService;

    // GET /api/items → Todos los items
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    // GET /api/items/1 → Item por ID
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    // GET /api/items/disponibles → Solo disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<ItemDTO>> getDisponibles() {
        return ResponseEntity.ok(itemService.findDisponibles());
    }

    // GET /api/items/category/1 → Items de una categoría
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ItemDTO>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(itemService.findByCategory(categoryId));
    }

    // GET /api/items/lab/1 → Items de un laboratorio
    @GetMapping("/lab/{labId}")
    public ResponseEntity<List<ItemDTO>> getByLab(@PathVariable Long labId) {
        return ResponseEntity.ok(itemService.findByLab(labId));
    }

    // GET /api/items/search?q=arduino → Búsqueda
    @GetMapping("/search")
    public ResponseEntity<List<ItemDTO>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(itemService.search(query));
    }

    // GET /api/items/status/DISPONIBLE → Filtrar por status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(itemService.findByStatus(status));
    }

    // GET /api/items/stats → Estadísticas del dashboard
    @GetMapping("/stats")
    public ResponseEntity<StatsDTO> getStats() {
        return ResponseEntity.ok(itemService.getStats());
    }
}