// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/dto/UserDTO.java
// ============================================================
package com.ibero.mindstock.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String numEstudiante;
    private String rol;
    // Nota: NO incluye passwordHash por seguridad
}