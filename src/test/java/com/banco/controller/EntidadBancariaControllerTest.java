package com.banco.controller;

import com.banco.dto.EntidadRequest;
import com.banco.dto.EntidadResponse;
import com.banco.exception.EntityAlreadyExistsException;
import com.banco.exception.EntityNotFoundException;
import com.banco.exception.GlobalExceptionHandler;
import com.banco.service.EntidadBancariaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EntidadBancariaController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("EntidadBancariaController — API Tests")
class EntidadBancariaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EntidadBancariaService service;

    private static final UUID ID = UUID.randomUUID();
    private static final String BASE_URL = "/v1/entidades";

    private EntidadRequest crearRequest() {
        return new EntidadRequest("0049", "Banco Santander", "BSCHESMMXXX", "España");
    }

    private EntidadResponse crearResponse() {
        return new EntidadResponse(ID, "0049", "Banco Santander", "BSCHESMMXXX", "España", true);
    }

    // =====================================================
    // POST — Alta
    // =====================================================

    @Nested
    @DisplayName("POST /v1/entidades")
    class Post {

        @Test
        @DisplayName("debería retornar 201 Created cuando el alta es exitosa")
        void crearExitoso() throws Exception {
            given(service.crear(any(EntidadRequest.class))).willReturn(crearResponse());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(crearRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.codigoEntidad", is("0049")))
                    .andExpect(jsonPath("$.nombre", is("Banco Santander")))
                    .andExpect(jsonPath("$.activo", is(true)));
        }

        @Test
        @DisplayName("debería retornar 400 Bad Request con campos inválidos")
        void crearValidacionFalla() throws Exception {
            EntidadRequest invalido = new EntidadRequest("", "", "", "");

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalido)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")));
        }

        @Test
        @DisplayName("debería retornar 409 Conflict si hay duplicado")
        void crearDuplicado() throws Exception {
            given(service.crear(any(EntidadRequest.class)))
                    .willThrow(new EntityAlreadyExistsException("Ya existe una entidad con el código: 0049"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(crearRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.message", is("Ya existe una entidad con el código: 0049")));
        }
    }

    // =====================================================
    // GET — Consultas
    // =====================================================

    @Nested
    @DisplayName("GET /v1/entidades")
    class Get {

        @Test
        @DisplayName("debería retornar 200 OK con la lista de entidades")
        void listarTodas() throws Exception {
            given(service.listarTodas()).willReturn(List.of(crearResponse()));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].codigoEntidad", is("0049")));
        }

        @Test
        @DisplayName("debería retornar 200 OK con entidad por ID")
        void buscarPorId() throws Exception {
            given(service.buscarPorId(ID)).willReturn(crearResponse());

            mockMvc.perform(get(BASE_URL + "/{id}", ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigoEntidad", is("0049")));
        }

        @Test
        @DisplayName("debería retornar 404 Not Found si no existe")
        void buscarNoExiste() throws Exception {
            given(service.buscarPorId(ID))
                    .willThrow(new EntityNotFoundException("No se encontró la entidad con ID: " + ID));

            mockMvc.perform(get(BASE_URL + "/{id}", ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }

        @Test
        @DisplayName("debería filtrar por país cuando se envía query param")
        void filtrarPorPais() throws Exception {
            given(service.listarPorPais("España")).willReturn(List.of(crearResponse()));

            mockMvc.perform(get(BASE_URL).param("pais", "España"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    // =====================================================
    // PUT — Modificación
    // =====================================================

    @Nested
    @DisplayName("PUT /v1/entidades/{id}")
    class Put {

        @Test
        @DisplayName("debería retornar 200 OK cuando la actualización es exitosa")
        void actualizarExitoso() throws Exception {
            EntidadResponse actualizado = new EntidadResponse(ID, "0049", "Nuevo Nombre", "BSCHESMMXXX", "Argentina", true);
            given(service.actualizar(eq(ID), any(EntidadRequest.class))).willReturn(actualizado);

            EntidadRequest request = new EntidadRequest("0049", "Nuevo Nombre", "BSCHESMMXXX", "Argentina");

            mockMvc.perform(put(BASE_URL + "/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre", is("Nuevo Nombre")))
                    .andExpect(jsonPath("$.pais", is("Argentina")));
        }
    }

    // =====================================================
    // PATCH — Baja lógica
    // =====================================================

    @Nested
    @DisplayName("PATCH /v1/entidades/{id}/desactivar")
    class Patch {

        @Test
        @DisplayName("debería retornar 204 No Content al desactivar")
        void desactivarExitoso() throws Exception {
            willDoNothing().given(service).desactivar(ID);

            mockMvc.perform(patch(BASE_URL + "/{id}/desactivar", ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("debería retornar 404 si la entidad no existe")
        void desactivarNoExiste() throws Exception {
            willThrow(new EntityNotFoundException("No se encontró la entidad con ID: " + ID))
                    .given(service).desactivar(ID);

            mockMvc.perform(patch(BASE_URL + "/{id}/desactivar", ID))
                    .andExpect(status().isNotFound());
        }
    }

    // =====================================================
    // DELETE — Baja física
    // =====================================================

    @Nested
    @DisplayName("DELETE /v1/entidades/{id}")
    class Delete {

        @Test
        @DisplayName("debería retornar 204 No Content al eliminar")
        void eliminarExitoso() throws Exception {
            willDoNothing().given(service).eliminar(ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("debería retornar 404 si la entidad no existe")
        void eliminarNoExiste() throws Exception {
            willThrow(new EntityNotFoundException("No se encontró la entidad con ID: " + ID))
                    .given(service).eliminar(ID);

            mockMvc.perform(delete(BASE_URL + "/{id}", ID))
                    .andExpect(status().isNotFound());
        }
    }
}
