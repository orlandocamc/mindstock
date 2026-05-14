// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/StatsDTO.java
//
// Estadísticas generales para el dashboard
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsDTO {
    private Long totalItems;
    private Long itemsDisponibles;
    private Long itemsPrestados;
    private Long solicitudesPendientes;
    private Long alertasNoLeidas;
}