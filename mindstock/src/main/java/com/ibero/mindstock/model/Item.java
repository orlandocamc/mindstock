// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/Item.java
//
// CONCEPTO POO: Herencia (extiende BaseEntity)
// CONCEPTO POO: Asociación (pertenece a Category y Lab)
// CONCEPTO POO: Encapsulamiento (lógica de negocio interna)
// ============================================================
package com.ibero.mindstock.model;

import com.ibero.mindstock.model.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    // -------------------------------------------------------
    // CONCEPTO POO: Asociación ManyToOne
    // Muchos Items pertenecen a una Category
    // -------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(name = "cantidad_total", nullable = false)
    private Integer cantidadTotal;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LoanRequest> solicitudes = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DetectionLog> detecciones = new ArrayList<>();

    // -------------------------------------------------------
    // CONCEPTO POO: Encapsulamiento de lógica de negocio
    // Los métodos controlan el estado interno del objeto
    // -------------------------------------------------------

    /**
     * Retira N unidades del inventario.
     * Actualiza el status automáticamente.
     */
    public boolean retirar(int cantidad) {
        if (cantidad > cantidadDisponible) {
            return false; // No hay suficientes
        }
        this.cantidadDisponible -= cantidad;
        actualizarStatus();
        return true;
    }

    /**
     * Devuelve N unidades al inventario.
     */
    public void devolver(int cantidad) {
        this.cantidadDisponible = Math.min(
                this.cantidadDisponible + cantidad,
                this.cantidadTotal
        );
        actualizarStatus();
    }

    /**
     * Pone el item en mantenimiento.
     */
    public void enviarAMantenimiento() {
        this.status = ItemStatus.MANTENIMIENTO;
    }

    /**
     * Actualiza el status basado en la cantidad disponible.
     */
    private void actualizarStatus() {
        if (this.status == ItemStatus.MANTENIMIENTO) return;
        this.status = cantidadDisponible > 0
                ? ItemStatus.DISPONIBLE
                : ItemStatus.AGOTADO;
    }

    public boolean estaDisponible() {
        return this.status == ItemStatus.DISPONIBLE && this.cantidadDisponible > 0;
    }

    @Override
    public String getDescripcionCompleta() {
        return String.format("Item: %s | Disponibles: %d/%d | Status: %s | Lab: %s",
                nombre, cantidadDisponible, cantidadTotal, status,
                lab != null ? lab.getNombre() : "Sin asignar");
    }
}