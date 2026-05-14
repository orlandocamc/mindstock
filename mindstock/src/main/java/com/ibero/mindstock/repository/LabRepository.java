// ARCHIVO: src/main/java/com/ibero/mindstock/repository/LabRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LabRepository extends JpaRepository<Lab, Long> {
    Optional<Lab> findByNombre(String nombre);
}