// ARCHIVO: src/main/java/com/ibero/mindstock/repository/CategoryRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNombre(String nombre);
}