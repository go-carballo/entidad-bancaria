# Entidad Bancaria Service

Microservicio REST con CRUD completo sobre entidades bancarias. Desarrollado con Spring Boot 4, Java 21 y Gradle.

## Stack Tecnologico

| Tecnologia | Version | Proposito |
|------------|---------|-----------|
| Java | 21 | Lenguaje (Records para DTOs) |
| Spring Boot | 4.0.5 | Framework |
| Spring Data JPA | - | Acceso a datos |
| Spring Validation | - | Validacion de entrada |
| H2 Database | In-memory | Base de datos de desarrollo |
| JUnit 5 + Mockito | - | Testing |
| Gradle | 9.4.1 | Build tool |

## Arquitectura

Arquitectura en capas (Layered Architecture):

```
Controller (REST)  →  Service (Negocio)  →  Repository (JPA)  →  H2
     ↕                      ↕
    DTOs               Excepciones
  (Records)          (@RestControllerAdvice)
```

### Estructura del proyecto

```
src/main/java/com/banco/
├── EntidadBancariaServiceApplication.java
├── config/
│   └── RestClientConfig.java          # Bean de RestClient para auto-consumo
├── controller/
│   ├── EntidadBancariaController.java # CRUD REST endpoints
│   └── ResumenController.java         # Endpoint de auto-consumo
├── dto/
│   ├── EntidadRequest.java            # Record de entrada (con validaciones)
│   ├── EntidadResponse.java           # Record de salida
│   └── ResumenResponse.java           # Record de resumen agregado
├── exception/
│   ├── EntityAlreadyExistsException.java
│   ├── EntityNotFoundException.java
│   ├── ErrorDetails.java              # Record de error estandarizado
│   └── GlobalExceptionHandler.java    # Manejo centralizado de excepciones
├── mapper/
│   └── EntidadMapper.java             # Mapper manual estatico
├── model/
│   └── EntidadBancaria.java           # Entidad JPA (equals/hashCode manual)
├── repository/
│   └── EntidadBancariaRepository.java # Spring Data JPA
└── service/
    ├── EntidadBancariaService.java    # Logica de negocio
    └── ResumenService.java            # Auto-consumo via RestClient
```

## Modelo de Dominio

**EntidadBancaria**

| Campo | Tipo | Constraint |
|-------|------|------------|
| id | UUID | PK, generado por Hibernate |
| codigoEntidad | String(10) | Unique, Not Null |
| nombre | String(100) | Not Null |
| bicSwift | String(11) | Unique, Not Null |
| pais | String(50) | Not Null |
| activo | Boolean | Not Null (default: true) |

## API Endpoints

### CRUD

| Metodo | Ruta | Status | Descripcion |
|--------|------|--------|-------------|
| POST | `/v1/entidades` | 201 Created | Alta de entidad |
| GET | `/v1/entidades` | 200 OK | Listar todas |
| GET | `/v1/entidades/{id}` | 200 OK | Buscar por ID |
| GET | `/v1/entidades?pais=Argentina` | 200 OK | Filtrar por pais |
| GET | `/v1/entidades?soloActivas=true` | 200 OK | Solo entidades activas |
| PUT | `/v1/entidades/{id}` | 200 OK | Modificacion completa |
| PATCH | `/v1/entidades/{id}/desactivar` | 204 No Content | Baja logica |
| DELETE | `/v1/entidades/{id}` | 204 No Content | Baja fisica |

### Auto-consumo

| Metodo | Ruta | Status | Descripcion |
|--------|------|--------|-------------|
| GET | `/v1/entidades/resumen` | 200 OK | Consume GET /v1/entidades via HTTP interno |

### Errores

| Status | Cuando |
|--------|--------|
| 400 Bad Request | Validacion de campos falla |
| 404 Not Found | Entidad no existe |
| 409 Conflict | Codigo o BIC/SWIFT duplicado |
| 500 Internal Server Error | Error inesperado (mensaje generico) |

Formato de error estandarizado:

```json
{
  "timestamp": "2026-04-21T16:40:08",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una entidad con el codigo: 0049"
}
```

## Ejecutar

### Requisitos

- Java 21+

### Iniciar la aplicacion

```bash
./gradlew bootRun
```

La aplicacion inicia en `http://localhost:8080`.

La consola H2 esta disponible en `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bancodb`, user: `sa`, sin password).

### Ejecutar tests

```bash
./gradlew test
```

36 tests en 3 niveles:

- **Unit** (Service + Mockito): 14 tests
- **Integration** (@DataJpaTest + H2): 9 tests
- **API** (MockMvc + @WebMvcTest): 13 tests

## Coleccion Postman

Importar `postman/Entidad-Bancaria-Service.postman_collection.json` en Postman.

La coleccion incluye 17 requests organizados en 6 carpetas con tests automaticos. Los IDs se capturan automaticamente en las Altas y se reutilizan en el resto de las requests.

Ejecutar en orden (1 a 6) usando el Collection Runner.

## Decisiones de Diseno

- **Records para DTOs**: Inmutables, compactos, sin Lombok.
- **equals/hashCode manual en la entidad**: Solo por ID, con hashCode constante (approach de Vlad Mihalcea). Evita inconsistencias en Sets/Maps cuando la entidad pasa de transient a managed.
- **Setters selectivos**: `codigoEntidad` es inmutable — no tiene setter. Es clave de negocio.
- **Bajas logicas**: Campo `activo` en vez de DELETE fisico (ambos disponibles).
- **Validacion de duplicados en doble capa**: Service (existsBy) + constraint DB (unique) como fallback.
- **RestClient para auto-consumo**: Sincrono, nativo de spring-boot-starter-web. Sin WebFlux.
- **Mapper manual**: Clase utilitaria estatica, sin MapStruct.
- **@Transactional(readOnly = true)** en consultas para optimizar dirty checking de Hibernate.
