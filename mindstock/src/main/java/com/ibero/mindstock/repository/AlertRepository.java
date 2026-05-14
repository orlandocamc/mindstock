// ARCHIVO: src/main/java/com/ibero/mindstock/repository/AlertRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByLaboratoristaIdAndLeidaFalse(Long laboratoristaId);
    List<Alert> findByLaboratoristaIdOrderByCreatedAtDesc(Long laboratoristaId);
    long countByLaboratoristaIdAndLeidaFalse(Long laboratoristaId);
}