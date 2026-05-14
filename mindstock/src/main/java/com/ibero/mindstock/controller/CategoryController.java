// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/controller/CategoryController.java
// ============================================================
package com.ibero.mindstock.controller;

import com.ibero.mindstock.dto.CategoryDTO;
import com.ibero.mindstock.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}