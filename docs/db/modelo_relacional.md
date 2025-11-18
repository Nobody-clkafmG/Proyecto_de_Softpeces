# Modelo relacional

El siguiente diagrama entidad–relación resume la estructura principal
de la base de datos de SoftPeces:

![Diagrama ER]

Las entidades principales son:

- ESTACION → TANQUE → LOTE → MUESTREO → FOTO (cadena de trazabilidad del lote).
- USERS, ROLES y USER_ROLES (gestión de usuarios y permisos).
- BITACORA (auditoría de acciones).
- TANQUE_LIMPIEZA (registro de labores de limpieza o mantenimiento).

