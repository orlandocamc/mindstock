// ARCHIVO: src/main/java/com/ibero/mindstock/repository/UserRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.User;
import com.ibero.mindstock.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNumEstudiante(String numEstudiante);
    Optional<User> findByRfidUid(String rfidUid);
    List<User> findByRol(Rol rol);
    boolean existsByEmail(String email);
}