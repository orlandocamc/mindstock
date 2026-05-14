// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/LoanRequest.java
//
// CONCEPTO POO: Asociación (conecta User con Item)
// CONCEPTO POO: Encapsulamiento (métodos que controlan el estado)
// ============================================================
package com.ibero.mindstock.model;

import com.ibero.mindstock.model.enums.DetectionMethod;
import com.ibero.mindstock.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loan_requests")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "detected_by", nullable = false)
    private DetectionMethod detectedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDIENTE;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    private Loan loan;

    // -------------------------------------------------------
    // Métodos de negocio
    // -------------------------------------------------------
    public boolean estaPendiente() {
        return this.status == RequestStatus.PENDIENTE;
    }

    public void aprobar() {
        this.status = RequestStatus.APROBADA;
    }

    public void rechazar() {
        this.status = RequestStatus.RECHAZADA;
    }

    public boolean fueDetectadaPorIA() {
        return this.detectedBy == DetectionMethod.AI_VISION;
    }

    @Override
    public String getDescripcionCompleta() {
        return String.format("Solicitud #%d: %s solicita %s (x%d) | Método: %s | Status: %s",
                getId(), user.getNombreCompleto(), item.getNombre(),
                cantidad, detectedBy, status);
    }
}