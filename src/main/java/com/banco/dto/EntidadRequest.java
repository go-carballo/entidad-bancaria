package com.banco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntidadRequest(

        @NotBlank(message = "El código de entidad es obligatorio")
        @Size(min = 4, max = 10, message = "El código de entidad debe tener entre 4 y 10 caracteres")
        String codigoEntidad,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String nombre,

        @NotBlank(message = "El código BIC/SWIFT es obligatorio")
        @Size(min = 8, max = 11, message = "El código BIC/SWIFT debe tener entre 8 y 11 caracteres")
        String bicSwift,

        @NotBlank(message = "El país es obligatorio")
        @Size(max = 50, message = "El país no puede exceder los 50 caracteres")
        String pais
) {
}
