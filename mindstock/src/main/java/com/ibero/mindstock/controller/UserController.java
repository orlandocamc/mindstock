// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/controller/UserController.java
// ============================================================
package com.ibero.mindstock.controller;

import com.ibero.mindstock.dto.UserDTO;
import com.ibero.mindstock.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /api/users/1
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // GET /api/users/rol/ALUMNO
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UserDTO>> getByRol(@PathVariable String rol) {
        return ResponseEntity.ok(userService.findByRol(rol));
    }

    // GET /api/users/laboratoristas
    @GetMapping("/laboratoristas")
    public ResponseEntity<List<UserDTO>> getLaboratoristas() {
        return ResponseEntity.ok(userService.findLaboratoristas());
    }
}