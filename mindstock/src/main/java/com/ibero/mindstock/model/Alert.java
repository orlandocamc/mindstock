// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/Alert.java
//
// Alertas que se envían al laboratorista
// ============================================================
package com.ibero.mindstock.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alerts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_id", nullable = false)
    private DetectionLog detectionLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorista_id", nullable = false)
    private User laboratorista;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    public void marcarComoLeida() {
        this.leida = true;
    }

    @Override
    public String getDescripcionCompleta() {
        return String.format("Alerta para %s: %s | Leída: %s",
                laboratorista.getNombreCompleto(), mensaje, leida ? "Sí" : "No");
    }
}