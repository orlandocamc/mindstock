// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/Category.java
// ============================================================
package com.ibero.mindstock.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    @Override
    public String getDescripcionCompleta() {
        return String.format("Categoría: %s (%d items)", nombre, items.size());
    }
}