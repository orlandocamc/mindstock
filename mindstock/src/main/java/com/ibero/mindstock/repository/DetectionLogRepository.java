// ARCHIVO: src/main/java/com/ibero/mindstock/repository/DetectionLogRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.DetectionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetectionLogRepository extends JpaRepository<DetectionLog, Long> {
    List<DetectionLog> findByItemId(Long itemId);
    List<DetectionLog> findTop10ByOrderByTimestampDesc();
}