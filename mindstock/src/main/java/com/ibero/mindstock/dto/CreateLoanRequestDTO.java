// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/CreateLoanRequestDTO.java
//
// DTO para cuando un alumno confirma un préstamo
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanRequestDTO {
    private Long userId;
    private Long itemId;
    private Integer cantidad;
    private String detectedBy;     // "AI_VISION" o "MANUAL"
    private Double confidenceScore;
}