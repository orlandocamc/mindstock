// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/ItemDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoryNombre;
    private Long categoryId;
    private String labNombre;
    private Long labId;
    private Integer cantidadTotal;
    private Integer cantidadDisponible;
    private String status;
    private String imagenUrl;
}
