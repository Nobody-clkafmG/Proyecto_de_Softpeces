
# Refactor no-invasivo alineado con el diagrama

Este commit añade *adaptadores* y *fachadas* para que el proyecto quede alineado con la estructura por capas del diagrama sin romper nada de lo existente.

## Paquetes nuevos

- `com.softpeces.arch.api.UIController`
- `com.softpeces.arch.application.{EvaluarLoteService, RegistrarMuestreoConFoto}`
- `com.softpeces.arch.infra.config.HikariDataSourceProvider` (envuelve SQLite; reemplazable por HikariCP)
- `com.softpeces.arch.infra.persistence.JdbcLoteRepository` (envuelve el repo actual)
- `com.softpeces.arch.infra.storage.{FotoStorage, LocalFotoStorage}`
- `com.softpeces.arch.domain.model.{Usuario, Clasificacion, Evaluacion, HistorialEvento}`

## Cómo usarlo

Desde cualquier controlador o vista puedes inyectar/crear `UIController` y llamar:

```java
var ui = new com.softpeces.arch.api.UIController();
String aptitud = ui.evaluarLote(loteId);
ui.registrarMuestreo(loteId, "E-01", 24.5, 7.3, "ok", Path.of("/ruta/foto.jpg"), Parte.DERECHA);
```

## Próximos pasos sugeridos

1. **Migrar** las clases existentes a los paquetes `arch.*` (cuando sea conveniente) y eliminar los adaptadores.
2. Sustituir `Database` por `HikariDataSourceProvider` si se cambia a Hikari/otra BD.
3. Extraer interfaces para `LoteRepository` y `MuestreoRepository` y mover sus implementaciones a `arch.infra.persistence`.
4. Centralizar almacenamiento de fotos usando `FotoStorage`.
