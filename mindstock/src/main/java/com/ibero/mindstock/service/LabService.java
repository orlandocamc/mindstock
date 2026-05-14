// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/service/LabService.java
// ============================================================
package com.ibero.mindstock.service;

import com.ibero.mindstock.dto.LabDTO;
import com.ibero.mindstock.model.Lab;
import com.ibero.mindstock.repository.LabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRepository labRepository;

    public List<LabDTO> findAll() {
        return labRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private LabDTO toDTO(Lab l) {
        return LabDTO.builder()
                .id(l.getId())
                .nombre(l.getNombre())
                .ubicacion(l.getUbicacion())
                .responsable(l.getResponsable())
                .itemCount(l.getItems() != null ? l.getItems().size() : 0)
                .build();
    }
}