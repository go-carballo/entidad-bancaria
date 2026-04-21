package com.banco.controller;

import com.banco.dto.EntidadRequest;
import com.banco.dto.EntidadResponse;
import com.banco.service.EntidadBancariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/entidades")
public class EntidadBancariaController {

    private final EntidadBancariaService service;

    public EntidadBancariaController(EntidadBancariaService service) {
        this.service = service;
    }

    // --- ALTA ---

    @PostMapping
    public ResponseEntity<EntidadResponse> crear(@Valid @RequestBody EntidadRequest request) {
        EntidadResponse response = service.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- CONSULTAS ---

    @GetMapping("/{id}")
    public ResponseEntity<EntidadResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EntidadResponse>> listar(
            @RequestParam(required = false) String pais,
            @RequestParam(required = false, defaultValue = "false") boolean soloActivas) {

        List<EntidadResponse> resultado;

        if (pais != null && !pais.isBlank()) {
            resultado = service.listarPorPais(pais);
        } else if (soloActivas) {
            resultado = service.listarActivas();
        } else {
            resultado = service.listarTodas();
        }

        return ResponseEntity.ok(resultado);
    }

    // --- MODIFICACIÓN ---

    @PutMapping("/{id}")
    public ResponseEntity<EntidadResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EntidadRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    // --- BAJA LÓGICA ---

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // --- BAJA FÍSICA ---

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
