package com.banco.service;

import com.banco.dto.EntidadRequest;
import com.banco.dto.EntidadResponse;
import com.banco.exception.EntityAlreadyExistsException;
import com.banco.exception.EntityNotFoundException;
import com.banco.mapper.EntidadMapper;
import com.banco.model.EntidadBancaria;
import com.banco.repository.EntidadBancariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EntidadBancariaService {

    private final EntidadBancariaRepository repository;

    public EntidadBancariaService(EntidadBancariaRepository repository) {
        this.repository = repository;
    }

    // --- ALTA ---

    @Transactional
    public EntidadResponse crear(EntidadRequest request) {
        validarDuplicados(request.codigoEntidad(), request.bicSwift());

        EntidadBancaria entity = EntidadMapper.toEntity(request);
        EntidadBancaria saved = repository.save(entity);

        return EntidadMapper.toResponse(saved);
    }

    // --- CONSULTAS ---

    @Transactional(readOnly = true)
    public EntidadResponse buscarPorId(UUID id) {
        EntidadBancaria entity = findOrThrow(id);
        return EntidadMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<EntidadResponse> listarTodas() {
        return repository.findAll().stream()
                .map(EntidadMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntidadResponse> listarActivas() {
        return repository.findByActivoTrue().stream()
                .map(EntidadMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntidadResponse> listarPorPais(String pais) {
        return repository.findByPais(pais).stream()
                .map(EntidadMapper::toResponse)
                .toList();
    }

    // --- MODIFICACIÓN ---

    @Transactional
    public EntidadResponse actualizar(UUID id, EntidadRequest request) {
        EntidadBancaria entity = findOrThrow(id);

        // Validar BIC/SWIFT duplicado solo si cambió
        if (!entity.getBicSwift().equals(request.bicSwift())
                && repository.existsByBicSwift(request.bicSwift())) {
            throw new EntityAlreadyExistsException(
                    "Ya existe una entidad con el código BIC/SWIFT: " + request.bicSwift());
        }

        EntidadMapper.updateEntity(entity, request);

        // No hace falta save() — dirty checking de Hibernate se encarga
        return EntidadMapper.toResponse(entity);
    }

    // --- BAJA LÓGICA ---

    @Transactional
    public void desactivar(UUID id) {
        EntidadBancaria entity = findOrThrow(id);
        entity.setActivo(false);
    }

    // --- BAJA FÍSICA (opcional, para el evaluador) ---

    @Transactional
    public void eliminar(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró la entidad con ID: " + id);
        }
        repository.deleteById(id);
    }

    // --- Métodos privados ---

    private EntidadBancaria findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la entidad con ID: " + id));
    }

    private void validarDuplicados(String codigoEntidad, String bicSwift) {
        if (repository.existsByCodigoEntidad(codigoEntidad)) {
            throw new EntityAlreadyExistsException(
                    "Ya existe una entidad con el código: " + codigoEntidad);
        }
        if (repository.existsByBicSwift(bicSwift)) {
            throw new EntityAlreadyExistsException(
                    "Ya existe una entidad con el código BIC/SWIFT: " + bicSwift);
        }
    }
}
