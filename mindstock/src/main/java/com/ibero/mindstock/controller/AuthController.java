package com.ibero.mindstock.controller;

import com.ibero.mindstock.dto.UserDTO;
import com.ibero.mindstock.model.User;
import com.ibero.mindstock.model.enums.Rol;
import com.ibero.mindstock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String nombre;
        private String apellido;
        private String email;
        private String numEstudiante;
        private String password;
        private String rfidUid;
    }

    @Data
    public static class RfidLoginRequest {
        private String rfidUid;
    }

    /**
     * POST /api/auth/login
     * Login con email + contraseña
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        return ResponseEntity.ok(buildAuthResponse(user));
    }

    /**
     * POST /api/auth/register
     * Registro de nuevo usuario (rol ALUMNO por defecto)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El email ya está registrado"));
        }

        if (req.getPassword() == null || req.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
        }

        User user = User.builder()
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .email(req.getEmail())
                .numEstudiante(req.getNumEstudiante())
                .passwordHash(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()))
                .rfidUid(req.getRfidUid() != null && !req.getRfidUid().isBlank()
                        ? req.getRfidUid() : null)
                .rol(Rol.ALUMNO)
                .build();

        user = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(user));
    }

    /**
     * POST /api/auth/rfid
     * Login rápido con tarjeta RFID
     */
    @PostMapping("/rfid")
    public ResponseEntity<?> loginRfid(@RequestBody RfidLoginRequest req) {
        if (req.getRfidUid() == null || req.getRfidUid().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "UID de RFID vacío"));
        }

        Optional<User> userOpt = userRepository.findByRfidUid(req.getRfidUid());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Tarjeta no registrada"));
        }

        return ResponseEntity.ok(buildAuthResponse(userOpt.get()));
    }

    private Map<String, Object> buildAuthResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("user", UserDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .email(user.getEmail())
                .numEstudiante(user.getNumEstudiante())
                .rol(user.getRol().name())
                .build());
        // Para producción real usar JWT. Para demo, token simple = userId
        response.put("token", "user-" + user.getId());
        return response;
    }
}