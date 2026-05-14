// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/AlertDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {
    private Long id;
    private String itemName;
    private String detectionAction;
    private Double confidence;
    private String mensaje;
    private Boolean leida;
    private String createdAt;
}