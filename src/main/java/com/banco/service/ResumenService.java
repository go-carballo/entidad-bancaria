package com.banco.service;

import com.banco.dto.EntidadResponse;
import com.banco.dto.ResumenResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResumenService {

    private final RestClient restClient;

    public ResumenService(RestClient restClient) {
        this.restClient = restClient;
    }

    public ResumenResponse obtenerResumen() {
        // Auto-consumo: llama al endpoint CRUD vía HTTP, no al Service directamente
        List<EntidadResponse> entidades = restClient.get()
                .uri("/v1/entidades")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        int total = entidades.size();

        int activas = (int) entidades.stream()
                .filter(EntidadResponse::activo)
                .count();

        int inactivas = total - activas;

        // Agrupa nombres de entidades por país
        Map<String, List<String>> porPais = entidades.stream()
                .collect(Collectors.groupingBy(
                        EntidadResponse::pais,
                        Collectors.mapping(EntidadResponse::nombre, Collectors.toList())
                ));

        return new ResumenResponse(total, activas, inactivas, porPais);
    }
}
