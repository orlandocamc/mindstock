// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/BaseEntity.java
//
// CONCEPTO POO: Clase abstracta
// Todas las entidades heredan de aquí. Define atributos comunes
// (id, timestamps) para evitar repetición de código.
// ============================================================
package com.ibero.mindstock.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass  // JPA: no crea tabla propia, los hijos heredan sus columnas
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // -------------------------------------------------------
    // CONCEPTO POO: Método concreto en clase abstracta
    // Los hijos lo heredan sin tener que reimplementarlo
    // -------------------------------------------------------
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------
    // CONCEPTO POO: Método abstracto
    // Cada entidad hija DEBE implementar su propia descripción
    // -------------------------------------------------------
    public abstract String getDescripcionCompleta();
}