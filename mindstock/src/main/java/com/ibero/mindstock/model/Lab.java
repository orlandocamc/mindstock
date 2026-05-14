// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/Lab.java
// ============================================================
package com.ibero.mindstock.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "labs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lab extends BaseEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String ubicacion;

    private String responsable;

    @OneToMany(mappedBy = "lab", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    @Override
    public String getDescripcionCompleta() {
        return String.format("Laboratorio: %s | Ubicación: %s | Responsable: %s",
                nombre, ubicacion, responsable);
    }
}