// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/Loan.java
//
// CONCEPTO POO: Ciclo de vida del objeto
// Un préstamo nace ACTIVO, puede pasar a DEVUELTO o VENCIDO
// ============================================================
package com.ibero.mindstock.model;

import com.ibero.mindstock.model.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private LoanRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private User approvedBy;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_devolucion_esperada", nullable = false)
    private LocalDateTime fechaDevolucionEsperada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVO;

    // -------------------------------------------------------
    // CONCEPTO POO: Lógica de negocio encapsulada
    // El objeto controla sus propias transiciones de estado
    // -------------------------------------------------------

    /**
     * Registra la devolución del préstamo
     */
    public void registrarDevolucion() {
        this.fechaDevolucionReal = LocalDateTime.now();
        this.status = LoanStatus.DEVUELTO;
        // Devolver items al inventario
        request.getItem().devolver(request.getCantidad());
    }

    /**
     * Marca como vencido si ya pasó la fecha de devolución
     */
    public boolean verificarVencimiento() {
        if (this.status == LoanStatus.ACTIVO
                && LocalDateTime.now().isAfter(fechaDevolucionEsperada)) {
            this.status = LoanStatus.VENCIDO;
            return true;
        }
        return false;
    }

    public boolean estaActivo() {
        return this.status == LoanStatus.ACTIVO;
    }

    public boolean estaVencido() {
        return this.status == LoanStatus.VENCIDO;
    }

    @Override
    public String getDescripcionCompleta() {
        return String.format("Préstamo #%d: %s → %s | Status: %s | Vence: %s",
                getId(),
                request.getUser().getNombreCompleto(),
                request.getItem().getNombre(),
                status,
                fechaDevolucionEsperada);
    }
}