// ARCHIVO: src/main/java/com/ibero/mindstock/repository/LoanRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.Loan;
import com.ibero.mindstock.model.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByRequestUserId(Long userId);
}