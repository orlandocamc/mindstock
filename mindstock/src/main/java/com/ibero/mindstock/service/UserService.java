// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/service/UserService.java
// ============================================================
package com.ibero.mindstock.service;

import com.ibero.mindstock.dto.UserDTO;
import com.ibero.mindstock.model.User;
import com.ibero.mindstock.model.enums.Rol;
import com.ibero.mindstock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return toDTO(user);
    }

    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        return toDTO(user);
    }

    public List<UserDTO> findByRol(String rol) {
        return userRepository.findByRol(Rol.valueOf(rol.toUpperCase())).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findLaboratoristas() {
        return userRepository.findByRol(Rol.LABORATORISTA).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .email(user.getEmail())
                .numEstudiante(user.getNumEstudiante())
                .rol(user.getRol().name())
                .build();
    }
}