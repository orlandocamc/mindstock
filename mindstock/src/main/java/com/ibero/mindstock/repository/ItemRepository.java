// ARCHIVO: src/main/java/com/ibero/mindstock/repository/ItemRepository.java
package com.ibero.mindstock.repository;

import com.ibero.mindstock.model.Item;
import com.ibero.mindstock.model.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(ItemStatus status);
    List<Item> findByCategoryId(Long categoryId);
    List<Item> findByLabId(Long labId);
    List<Item> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT i FROM Item i WHERE i.cantidadDisponible > 0 AND i.status = 'DISPONIBLE'")
    List<Item> findAllDisponibles();

    @Query("SELECT i FROM Item i WHERE i.cantidadDisponible = 0")
    List<Item> findAllAgotados();
}