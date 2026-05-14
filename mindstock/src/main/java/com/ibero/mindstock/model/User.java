// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/model/User.java
//
// CONCEPTO POO: Herencia (extiende BaseEntity)
// CONCEPTO POO: Encapsulamiento (campos privados + getters/setters via Lombok)
// CONCEPTO POO: Polimorfismo (implementa getDescripcionCompleta)
// ============================================================
package com.ibero.mindstock.model;

import com.ibero.mindstock.model.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "num_estudiante", unique = true)
    private String numEstudiante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "rfid_uid", unique = true)
    private String rfidUid;

    // -------------------------------------------------------
    // CONCEPTO POO: Composición (User TIENE muchos LoanRequests)
    // Un User no puede existir sin su lista de solicitudes
    // -------------------------------------------------------
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LoanRequest> solicitudes = new ArrayList<>();

    @OneToMany(mappedBy = "laboratorista", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Alert> alertas = new ArrayList<>();

    // -------------------------------------------------------
    // CONCEPTO POO: Polimorfismo - implementación del método abstracto
    // -------------------------------------------------------
    @Override
    public String getDescripcionCompleta() {
        return String.format("Usuario: %s %s | Rol: %s | ID: %s",
                nombre, apellido, rol, numEstudiante);
    }

    // -------------------------------------------------------
    // Métodos de utilidad
    // -------------------------------------------------------
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean esLaboratorista() {
        return this.rol == Rol.LABORATORISTA || this.rol == Rol.ADMIN;
    }

    public boolean esAdmin() {
        return this.rol == Rol.ADMIN;
    }
}