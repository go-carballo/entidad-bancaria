package com.banco.repository;

import com.banco.model.EntidadBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntidadBancariaRepository extends JpaRepository<EntidadBancaria, UUID> {

    boolean existsByCodigoEntidad(String codigoEntidad);

    boolean existsByBicSwift(String bicSwift);

    Optional<EntidadBancaria> findByCodigoEntidad(String codigoEntidad);

    List<EntidadBancaria> findByPais(String pais);

    List<EntidadBancaria> findByActivoTrue();
}
