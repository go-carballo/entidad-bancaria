package com.banco.controller;

import com.banco.dto.ResumenResponse;
import com.banco.service.ResumenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/entidades")
public class ResumenController {

    private final ResumenService resumenService;

    public ResumenController(ResumenService resumenService) {
        this.resumenService = resumenService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenResponse> obtenerResumen() {
        return ResponseEntity.ok(resumenService.obtenerResumen());
    }
}
