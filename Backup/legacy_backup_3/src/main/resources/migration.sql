-- Migración para agregar nuevas columnas a la tabla TANQUE
-- Ejecutar este script si ya tienes datos en la base de datos

-- Agregar las nuevas columnas a la tabla TANQUE
ALTER TABLE TANQUE ADD COLUMN TIPO_PEZ TEXT;
ALTER TABLE TANQUE ADD COLUMN PECES_APROX INTEGER;
ALTER TABLE TANQUE ADD COLUMN FECHA_INICIO TEXT;

