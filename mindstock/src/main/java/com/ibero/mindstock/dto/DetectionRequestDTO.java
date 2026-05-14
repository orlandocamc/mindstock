// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/DetectionRequestDTO.java
//
// Este DTO lo envía el módulo de Python cuando detecta un objeto
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionRequestDTO {
    private Long itemId;
    private String action;       // "RETIRO" o "DEVOLUCION"
    private Double confidence;
    private String frameUrl;
}