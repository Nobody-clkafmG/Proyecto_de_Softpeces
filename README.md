# SoftPeces – Gestión de piscicultura con IA

Aplicación de escritorio desarrollada en **Java/JavaFX** para gestionar estaciones, tanques, lotes y muestreos de tilapia. 
Integra un modelo de **IA en formato ONNX** para analizar fotografías de la carne y apoyar la decisión sobre la aptitud del lote.

## 🚀 Funcionalidades principales

- Gestión de usuarios y roles (administrador, operario, inspector de calidad).
- Registro de estaciones y tanques.
- Creación y seguimiento de lotes de tilapia.
- Registro de muestreos con evidencia fotográfica.
- Análisis automático de fotos con un modelo IA (ONNX).
- Reportes de productividad y trazabilidad.

## 🧱 Arquitectura

El proyecto está organizado en capas:

- **UI (JavaFX)** – Vistas y controladores.
- **Aplicación** – Casos de uso (servicios de aplicación).
- **Dominio** – Entidades, agregados y lógica de negocio.
- **Infraestructura** – Acceso a base de datos, almacenamiento de fotos, auditoría e IA.

Más detalles en `docs/architecture.md` y diagramas UML en `docs/uml/`.

## 📚 Documentación

Toda la documentación del proyecto se encuentra en la carpeta `docs/`:

- `docs/requirements/HU_RF.md` – Historias de usuario y requerimientos.
- `docs/requirements/arquitectura.md` – Vista de arquitectura del sistema.
- `docs/uml/uml_index.md` – Índice de diagramas UML y trazabilidad.
- `docs/ui-mockups/` – Mockups de la interfaz.
- `docs/db/` – Modelo relacional y diagrama ER.
- `docs/sprints/` – Informes de los sprints (Scrum).

## 🛠 Tecnologías

- Java 17 (o la versión que uses)
- JavaFX
- SQLite (u otra BD que estés usando)
- Gradle
- ONNX Runtime (para el modelo de IA)

## ▶️ Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/TU-USUARIO/TU-REPO.git
   cd TU-REPO
