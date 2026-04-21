package com.banco.service;

import com.banco.dto.EntidadRequest;
import com.banco.dto.EntidadResponse;
import com.banco.exception.EntityAlreadyExistsException;
import com.banco.exception.EntityNotFoundException;
import com.banco.model.EntidadBancaria;
import com.banco.repository.EntidadBancariaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntidadBancariaService — Unit Tests")
class EntidadBancariaServiceTest {

    @Mock
    private EntidadBancariaRepository repository;

    @InjectMocks
    private EntidadBancariaService service;

    private static final String CODIGO = "0049";
    private static final String NOMBRE = "Banco Santander";
    private static final String BIC = "BSCHESMMXXX";
    private static final String PAIS = "España";

    private EntidadRequest crearRequest() {
        return new EntidadRequest(CODIGO, NOMBRE, BIC, PAIS);
    }

    private EntidadBancaria crearEntidad() {
        return new EntidadBancaria(CODIGO, NOMBRE, BIC, PAIS);
    }

    // =====================================================
    // ALTA
    // =====================================================

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("debería crear una entidad cuando no hay duplicados")
        void crearExitoso() {
            EntidadRequest request = crearRequest();
            EntidadBancaria entity = crearEntidad();

            given(repository.existsByCodigoEntidad(CODIGO)).willReturn(false);
            given(repository.existsByBicSwift(BIC)).willReturn(false);
            given(repository.save(any(EntidadBancaria.class))).willReturn(entity);

            EntidadResponse response = service.crear(request);

            assertThat(response.codigoEntidad()).isEqualTo(CODIGO);
            assertThat(response.nombre()).isEqualTo(NOMBRE);
            assertThat(response.bicSwift()).isEqualTo(BIC);
            assertThat(response.pais()).isEqualTo(PAIS);

            then(repository).should().save(any(EntidadBancaria.class));
        }

        @Test
        @DisplayName("debería lanzar excepción si el código de entidad ya existe")
        void crearDuplicadoCodigo() {
            EntidadRequest request = crearRequest();
            given(repository.existsByCodigoEntidad(CODIGO)).willReturn(true);

            assertThatThrownBy(() -> service.crear(request))
                    .isInstanceOf(EntityAlreadyExistsException.class)
                    .hasMessageContaining(CODIGO);

            then(repository).should(never()).save(any());
        }

        @Test
        @DisplayName("debería lanzar excepción si el BIC/SWIFT ya existe")
        void crearDuplicadoBic() {
            EntidadRequest request = crearRequest();
            given(repository.existsByCodigoEntidad(CODIGO)).willReturn(false);
            given(repository.existsByBicSwift(BIC)).willReturn(true);

            assertThatThrownBy(() -> service.crear(request))
                    .isInstanceOf(EntityAlreadyExistsException.class)
                    .hasMessageContaining(BIC);

            then(repository).should(never()).save(any());
        }
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("debería retornar la entidad cuando existe")
        void buscarExitoso() {
            UUID id = UUID.randomUUID();
            EntidadBancaria entity = crearEntidad();
            given(repository.findById(id)).willReturn(Optional.of(entity));

            EntidadResponse response = service.buscarPorId(id);

            assertThat(response.codigoEntidad()).isEqualTo(CODIGO);
        }

        @Test
        @DisplayName("debería lanzar excepción cuando no existe")
        void buscarNoExiste() {
            UUID id = UUID.randomUUID();
            given(repository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(id))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    @DisplayName("listarTodas()")
    class ListarTodas {

        @Test
        @DisplayName("debería retornar lista vacía cuando no hay entidades")
        void listarVacia() {
            given(repository.findAll()).willReturn(List.of());

            List<EntidadResponse> resultado = service.listarTodas();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("debería retornar todas las entidades")
        void listarConDatos() {
            EntidadBancaria e1 = new EntidadBancaria("0049", "Santander", "BSCHESMMXXX", "España");
            EntidadBancaria e2 = new EntidadBancaria("0075", "Popular", "POPUESMMXXX", "España");
            given(repository.findAll()).willReturn(List.of(e1, e2));

            List<EntidadResponse> resultado = service.listarTodas();

            assertThat(resultado).hasSize(2);
        }
    }

    // =====================================================
    // MODIFICACIÓN
    // =====================================================

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("debería actualizar campos mutables sin tocar codigoEntidad")
        void actualizarExitoso() {
            UUID id = UUID.randomUUID();
            EntidadBancaria entity = crearEntidad();
            EntidadRequest request = new EntidadRequest(CODIGO, "Nuevo Nombre", BIC, "Argentina");

            given(repository.findById(id)).willReturn(Optional.of(entity));

            EntidadResponse response = service.actualizar(id, request);

            assertThat(response.nombre()).isEqualTo("Nuevo Nombre");
            assertThat(response.pais()).isEqualTo("Argentina");
            assertThat(response.codigoEntidad()).isEqualTo(CODIGO);
        }

        @Test
        @DisplayName("debería lanzar excepción si el nuevo BIC/SWIFT ya existe")
        void actualizarBicDuplicado() {
            UUID id = UUID.randomUUID();
            EntidadBancaria entity = crearEntidad();
            String nuevoBic = "OTROBICOXXXX";
            EntidadRequest request = new EntidadRequest(CODIGO, NOMBRE, nuevoBic, PAIS);

            given(repository.findById(id)).willReturn(Optional.of(entity));
            given(repository.existsByBicSwift(nuevoBic)).willReturn(true);

            assertThatThrownBy(() -> service.actualizar(id, request))
                    .isInstanceOf(EntityAlreadyExistsException.class)
                    .hasMessageContaining(nuevoBic);
        }

        @Test
        @DisplayName("no debería validar BIC si no cambió")
        void actualizarMismoBic() {
            UUID id = UUID.randomUUID();
            EntidadBancaria entity = crearEntidad();
            EntidadRequest request = new EntidadRequest(CODIGO, "Otro Nombre", BIC, PAIS);

            given(repository.findById(id)).willReturn(Optional.of(entity));

            EntidadResponse response = service.actualizar(id, request);

            assertThat(response.nombre()).isEqualTo("Otro Nombre");
            then(repository).should(never()).existsByBicSwift(any());
        }
    }

    // =====================================================
    // BAJAS
    // =====================================================

    @Nested
    @DisplayName("desactivar()")
    class Desactivar {

        @Test
        @DisplayName("debería setear activo en false")
        void desactivarExitoso() {
            UUID id = UUID.randomUUID();
            EntidadBancaria entity = crearEntidad();
            given(repository.findById(id)).willReturn(Optional.of(entity));

            service.desactivar(id);

            assertThat(entity.getActivo()).isFalse();
        }

        @Test
        @DisplayName("debería lanzar excepción si no existe")
        void desactivarNoExiste() {
            UUID id = UUID.randomUUID();
            given(repository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.desactivar(id))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("eliminar()")
    class Eliminar {

        @Test
        @DisplayName("debería eliminar cuando existe")
        void eliminarExitoso() {
            UUID id = UUID.randomUUID();
            given(repository.existsById(id)).willReturn(true);

            service.eliminar(id);

            then(repository).should().deleteById(id);
        }

        @Test
        @DisplayName("debería lanzar excepción si no existe")
        void eliminarNoExiste() {
            UUID id = UUID.randomUUID();
            given(repository.existsById(id)).willReturn(false);

            assertThatThrownBy(() -> service.eliminar(id))
                    .isInstanceOf(EntityNotFoundException.class);

            then(repository).should(never()).deleteById(any());
        }
    }
}
