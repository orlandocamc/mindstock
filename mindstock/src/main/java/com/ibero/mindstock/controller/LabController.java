// ============================================================
// ARCHIVO: src/main/java/com/ibero/mindstock/controller/LabController.java
// ============================================================
package com.ibero.mindstock.controller;

import com.ibero.mindstock.dto.LabDTO;
import com.ibero.mindstock.service.LabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LabController {

    private final LabService labService;

    @GetMapping
    public ResponseEntity<List<LabDTO>> getAll() {
        return ResponseEntity.ok(labService.findAll());
    }
}