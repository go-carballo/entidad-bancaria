package com.banco.dto;

import java.util.List;
import java.util.Map;

public record ResumenResponse(
        int totalEntidades,
        int activas,
        int inactivas,
        Map<String, List<String>> entidadesPorPais
) {
}
