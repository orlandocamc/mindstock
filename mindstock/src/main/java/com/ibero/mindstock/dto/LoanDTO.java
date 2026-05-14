// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/LoanDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDTO {
    private Long id;
    private Long requestId;
    private String userName;
    private String itemName;
    private Integer cantidad;
    private String approvedByName;
    private String fechaPrestamo;
    private String fechaDevolucionEsperada;
    private String fechaDevolucionReal;
    private String status;
}