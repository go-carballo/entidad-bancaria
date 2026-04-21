package com.banco.repository;

import com.banco.model.EntidadBancaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("EntidadBancariaRepository — Integration Tests")
class EntidadBancariaRepositoryTest {

    @Autowired
    private EntidadBancariaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private EntidadBancaria entidadGuardada;

    @BeforeEach
    void setUp() {
        EntidadBancaria entidad = new EntidadBancaria("0049", "Banco Santander", "BSCHESMMXXX", "España");
        entidadGuardada = entityManager.persistAndFlush(entidad);
    }

    @Test
    @DisplayName("existsByCodigoEntidad debería retornar true si existe")
    void existsByCodigoEntidadTrue() {
        assertThat(repository.existsByCodigoEntidad("0049")).isTrue();
    }

    @Test
    @DisplayName("existsByCodigoEntidad debería retornar false si no existe")
    void existsByCodigoEntidadFalse() {
        assertThat(repository.existsByCodigoEntidad("9999")).isFalse();
    }

    @Test
    @DisplayName("existsByBicSwift debería retornar true si existe")
    void existsByBicSwiftTrue() {
        assertThat(repository.existsByBicSwift("BSCHESMMXXX")).isTrue();
    }

    @Test
    @DisplayName("existsByBicSwift debería retornar false si no existe")
    void existsByBicSwiftFalse() {
        assertThat(repository.existsByBicSwift("NOEXISTEXXXX")).isFalse();
    }

    @Test
    @DisplayName("findByCodigoEntidad debería retornar la entidad")
    void findByCodigoEntidad() {
        Optional<EntidadBancaria> resultado = repository.findByCodigoEntidad("0049");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Banco Santander");
    }

    @Test
    @DisplayName("findByPais debería retornar entidades del país")
    void findByPais() {
        EntidadBancaria otra = new EntidadBancaria("0075", "Banco Popular", "POPUESMMXXX", "España");
        entityManager.persistAndFlush(otra);

        EntidadBancaria argentina = new EntidadBancaria("0017", "Banco Nación", "NACABORXXXX", "Argentina");
        entityManager.persistAndFlush(argentina);

        List<EntidadBancaria> espanolas = repository.findByPais("España");

        assertThat(espanolas).hasSize(2);
        assertThat(espanolas).extracting(EntidadBancaria::getPais)
                .containsOnly("España");
    }

    @Test
    @DisplayName("findByActivoTrue debería retornar solo entidades activas")
    void findByActivoTrue() {
        EntidadBancaria inactiva = new EntidadBancaria("0075", "Banco Inactivo", "INACESMMXXX", "España");
        inactiva.setActivo(false);
        entityManager.persistAndFlush(inactiva);

        List<EntidadBancaria> activas = repository.findByActivoTrue();

        assertThat(activas).hasSize(1);
        assertThat(activas.get(0).getCodigoEntidad()).isEqualTo("0049");
    }

    @Test
    @DisplayName("la base de datos debería rechazar código de entidad duplicado")
    void uniqueConstraintCodigoEntidad() {
        EntidadBancaria duplicada = new EntidadBancaria("0049", "Otro Banco", "OTROBICXXXXX", "Argentina");

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(duplicada);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("la base de datos debería rechazar BIC/SWIFT duplicado")
    void uniqueConstraintBicSwift() {
        EntidadBancaria duplicada = new EntidadBancaria("9999", "Otro Banco", "BSCHESMMXXX", "Argentina");

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(duplicada);
        }).isInstanceOf(Exception.class);
    }
}
