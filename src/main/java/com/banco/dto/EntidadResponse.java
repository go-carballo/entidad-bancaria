package com.banco.dto;

import java.util.UUID;

public record EntidadResponse(
        UUID id,
        String codigoEntidad,
        String nombre,
        String bicSwift,
        String pais,
        Boolean activo
) {
}
