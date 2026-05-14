// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/CategoryDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer itemCount;
}