// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/LabDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabDTO {
    private Long id;
    private String nombre;
    private String ubicacion;
    private String responsable;
    private Integer itemCount;
}