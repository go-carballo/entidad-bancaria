package com.banco.mapper;

import com.banco.dto.EntidadRequest;
import com.banco.dto.EntidadResponse;
import com.banco.model.EntidadBancaria;

public final class EntidadMapper {

    private EntidadMapper() {
        // Clase utilitaria — no instanciable
    }

    public static EntidadBancaria toEntity(EntidadRequest request) {
        return new EntidadBancaria(
                request.codigoEntidad(),
                request.nombre(),
                request.bicSwift(),
                request.pais()
        );
    }

    public static EntidadResponse toResponse(EntidadBancaria entity) {
        return new EntidadResponse(
                entity.getId(),
                entity.getCodigoEntidad(),
                entity.getNombre(),
                entity.getBicSwift(),
                entity.getPais(),
                entity.getActivo()
        );
    }

    public static void updateEntity(EntidadBancaria entity, EntidadRequest request) {
        entity.setNombre(request.nombre());
        entity.setBicSwift(request.bicSwift());
        entity.setPais(request.pais());
        // codigoEntidad NO se actualiza — es inmutable como clave de negocio
    }
}
