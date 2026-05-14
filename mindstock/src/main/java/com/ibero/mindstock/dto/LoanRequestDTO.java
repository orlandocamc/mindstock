// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/LoanRequestDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequestDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long itemId;
    private String itemName;
    private Integer cantidad;
    private String detectedBy;
    private String status;
    private Double confidenceScore;
    private String createdAt;
}