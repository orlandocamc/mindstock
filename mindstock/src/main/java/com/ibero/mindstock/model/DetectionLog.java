// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/DetectionLog.java
//
// Registra cada vez que la IA detecta un objeto
// ============================================================
package com.ibero.mindstock.model;

import com.ibero.mindstock.model.enums.DetectionAction;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "detection_logs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionAction action;

    @Column(nullable = false)
    private Double confidence;

    @Column(name = "frame_url")
    private String frameUrl;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @OneToOne(mappedBy = "detectionLog", cascade = CascadeType.ALL)
    private Alert alert;

    public boolean esConfiable() {
        return this.confidence >= 0.85;
    }

    @Override
    public String getDescripcionCompleta() {
        return String.format("Detección: %s de %s | Confianza: %.1f%% | %s",
                action, item.getNombre(), confidence * 100, timestamp);
    }
}