// ARCHIVO: src/main/java/com/ibero/mindstock/repository/LoanRequestRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.LoanRequest;
import com.ibero.mindstock.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByStatus(RequestStatus status);
    List<LoanRequest> findByUserId(Long userId);
    List<LoanRequest> findByItemId(Long itemId);
}