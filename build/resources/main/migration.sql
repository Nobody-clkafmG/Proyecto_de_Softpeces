-- Migración para agregar nuevas columnas a la tabla TANQUE
-- Ejecutar este script si ya tienes datos en la base de datos

-- Agregar las nuevas columnas a la tabla TANQUE
ALTER TABLE TANQUE ADD COLUMN TIPO_PEZ TEXT;
ALTER TABLE TANQUE ADD COLUMN PECES_APROX INTEGER;
ALTER TABLE TANQUE ADD COLUMN FECHA_INICIO TEXT;

-- Historial de limpiezas por tanque
CREATE TABLE IF NOT EXISTS TANQUE_LIMPIEZA (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  tanque_id   INTEGER NOT NULL,
  fecha       TEXT    NOT NULL,              -- ISO yyyy-MM-dd
  responsable TEXT    NOT NULL,
  descripcion TEXT    NOT NULL,
  created_at  TEXT    DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (tanque_id) REFERENCES TANQUE(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_limpieza_tanque_fecha
  ON TANQUE_LIMPIEZA(tanque_id, fecha);
