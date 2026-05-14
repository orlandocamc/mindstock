// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/service/CategoryService.java
// ============================================================
package com.ibero.mindstock.service;

import com.ibero.mindstock.dto.CategoryDTO;
import com.ibero.mindstock.model.Category;
import com.ibero.mindstock.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .itemCount(c.getItems() != null ? c.getItems().size() : 0)
                .build();
    }
}