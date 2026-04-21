package com.banco.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "entidades_bancarias")
public class EntidadBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_entidad", unique = true, nullable = false, length = 10)
    private String codigoEntidad;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "bic_swift", unique = true, nullable = false, length = 11)
    private String bicSwift;

    @Column(name = "pais", nullable = false, length = 50)
    private String pais;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    protected EntidadBancaria() {
        // Constructor protegido requerido por JPA/Hibernate
    }

    public EntidadBancaria(String codigoEntidad, String nombre, String bicSwift, String pais) {
        this.codigoEntidad = codigoEntidad;
        this.nombre = nombre;
        this.bicSwift = bicSwift;
        this.pais = pais;
        this.activo = true;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public String getCodigoEntidad() {
        return codigoEntidad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getBicSwift() {
        return bicSwift;
    }

    public String getPais() {
        return pais;
    }

    public Boolean getActivo() {
        return activo;
    }

    // --- Setters (solo los campos mutables) ---

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setBicSwift(String bicSwift) {
        this.bicSwift = bicSwift;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // --- equals & hashCode: SOLO por ID ---
    // En JPA, dos entidades son iguales si son de la misma clase y tienen el mismo ID.
    // NO usamos @Data de Lombok porque compara todos los campos y rompe
    // la consistencia cuando la entidad pasa de transient a managed.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntidadBancaria that = (EntidadBancaria) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // Retornamos una constante para que el hash sea estable
        // entre los estados transient (id=null) y managed (id=UUID).
        // Esto es el approach recomendado por Vlad Mihalcea.
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EntidadBancaria{" +
                "id=" + id +
                ", codigoEntidad='" + codigoEntidad + '\'' +
                ", nombre='" + nombre + '\'' +
                ", bicSwift='" + bicSwift + '\'' +
                ", pais='" + pais + '\'' +
                ", activo=" + activo +
                '}';
    }
}
