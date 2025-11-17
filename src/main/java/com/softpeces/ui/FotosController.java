package com.softpeces.ui;

import com.softpeces.catalog.Estacion;
import com.softpeces.catalog.EstacionRepository;
import com.softpeces.catalog.Tanque;
import com.softpeces.catalog.TanqueRepository;
import com.softpeces.domain.Foto;
import com.softpeces.domain.MuestreoRow;
import com.softpeces.domain.Parte;
import com.softpeces.domain.EstadoFoto;
import com.softpeces.qc.ImageQuality;
import com.softpeces.repo.FotoRepository;
import com.softpeces.domain.Lote;
import com.softpeces.repo.LoteRepository;
import com.softpeces.repo.MuestreoRepository;
import com.softpeces.infra.Database;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FotosController {

    @FXML private ComboBox<Estacion> cbEstacion;
    @FXML private ComboBox<Tanque> cbTanque;
    @FXML private TableView<Foto> tbl;
    @FXML private TableColumn<Foto,Integer> colId;
    @FXML private TableColumn<Foto,String> colParte;
    @FXML private TableColumn<Foto,String> colRuta;
    @FXML private TableColumn<Foto,String> colQC;
    @FXML private TableColumn<Foto,String> colEstado;
    @FXML private TableColumn<Foto,String> colLabel;
    @FXML private TableColumn<Foto,Double> colProb;
    @FXML private TableColumn<Foto,String> colMsg;
    @FXML private Label lblMsg;

    private final FotoRepository fotos = new FotoRepository();
    private final EstacionRepository estRepo = new EstacionRepository();
    private final TanqueRepository tanqRepo = new TanqueRepository();
    private final LoteRepository loteRepo = new LoteRepository();
    private final MuestreoRepository muestreoRepo = new MuestreoRepository();

    // Procesador de IA (actualizado para aceptar "parte")
    private final com.softpeces.ia.FotoProcessor processor = new com.softpeces.ia.FotoProcessor();

    private MuestreoRow lastMuestreo;

    @FXML
    public void initialize() {
        // Estaciones
        cbEstacion.setItems(FXCollections.observableArrayList(estRepo.findAll()));
        cbEstacion.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Estacion e) { return e==null? "" : (e.id()+" - "+e.nombre()); }
            public Estacion fromString(String s) { return null; }
        });
        cbEstacion.getSelectionModel().selectedItemProperty().addListener((o,a,b)-> {
            loadTanques();
            updateTableForSelection();
        });

        // Tanques
        cbTanque.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Tanque t) { return t==null? "" : (t.codigo()+" - "+t.tipoPez()); }
            public Tanque fromString(String s) { return null; }
        });
        cbTanque.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> updateTableForSelection());

        // Tabla
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colParte.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().parte().name()));
        colRuta.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().ruta()));
        colQC.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().qcOk()? "OK" : "FALLA"));
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().estado().name()));
        colLabel.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().label()));
        colProb.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().prob()));
        colMsg.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().mensajeError()));

        tbl.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        lblMsg.setText("");
        updateTableForSelection();
    }

    private void loadTanques() {
        var e = cbEstacion.getValue();
        if (e==null) {
            cbTanque.setItems(FXCollections.observableArrayList());
            cbTanque.getSelectionModel().clearSelection();
            return;
        }
        var tanques = FXCollections.observableArrayList(tanqRepo.findByEstacion(e.id()));
        cbTanque.setItems(tanques);
        if (!tanques.isEmpty()) {
            cbTanque.getSelectionModel().selectFirst();
        } else {
            cbTanque.getSelectionModel().clearSelection();
        }
    }

    private void recargarTabla() {
        if (lastMuestreo == null) {
            updateTableForSelection();
            return;
        }
        tbl.setItems(FXCollections.observableArrayList(fotos.findByMuestreo(lastMuestreo.id())));
    }

    private void updateTableForSelection() {
        Estacion estacion = cbEstacion.getValue();
        Tanque tanque = cbTanque.getValue();
        if (estacion == null || tanque == null) {
            lastMuestreo = null;
            tbl.setItems(FXCollections.observableArrayList());
            return;
        }
        lastMuestreo = findLatestMuestreo(estacion.id(), tanque.id());
        if (lastMuestreo == null) {
            tbl.setItems(FXCollections.observableArrayList());
        } else {
            tbl.setItems(FXCollections.observableArrayList(fotos.findByMuestreo(lastMuestreo.id())));
        }
    }

    @FXML
    public void onAgregarFotos() {
        // Seleccionar imágenes
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg","*.jpeg","*.png","*.bmp")
        );
        List<File> files = fc.showOpenMultipleDialog(tbl.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        // Carpeta "imagenes"
        Path imagenesDir = Paths.get("imagenes");
        if (!Files.exists(imagenesDir)) {
            try {
                Files.createDirectories(imagenesDir);
            } catch (IOException e) {
                lblMsg.setText("Error creando carpeta de imágenes: " + e.getMessage());
                return;
            }
        }

        // Info previa
        StringBuilder infoImagenes = new StringBuilder();
        infoImagenes.append("📸 Imágenes seleccionadas para subir:\n\n");
        for (File file : files) {
            infoImagenes.append("✅ ").append(file.getName()).append("\n");
            infoImagenes.append("   📁 Ruta original: ").append(file.getAbsolutePath()).append("\n");
            infoImagenes.append("   📏 Tamaño: ").append(file.length() / 1024).append(" KB\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Imágenes Seleccionadas");
        alert.setHeaderText("Se han seleccionado " + files.size() + " imagen(es) para subir");
        alert.setContentText(infoImagenes.toString());
        alert.showAndWait();

        // Muestreo
        MuestreoRow muestreo = getOrCreateMuestreo();
        lastMuestreo = muestreo;
        if (muestreo == null) {
            lblMsg.setText("❌ Error: No hay muestreos disponibles. Crea un muestreo primero.");
            return;
        }

        String tipoPez = getTipoPez(muestreo.tanqueId());

        // Diálogo de PARTE (enum del dominio; aquí no aparece "AUTO" porque es para registrar la foto)
        Parte parte = askParte();
        if (parte == null) return;

        StringBuilder resultado = new StringBuilder();
        resultado.append("Imagen seleccionada correctamente (Muestreo automático):\n\n");

        var existentes = fotos.findByMuestreo(muestreo.id());
        int baseIndex = existentes.size();
        int ok=0, fail=0, qcWarnings=0;
        for (int i = 0; i < files.size(); i++) {
            File originalFile = files.get(i);
            var qc = ImageQuality.check(originalFile.getAbsolutePath());
            boolean qcOk = qc.ok();

            int consecutivo = baseIndex + i;
            String nuevoNombre = muestreo.estacionId() + "_" + muestreo.tanqueId() + "_" + String.format("%02d", consecutivo) + "." + getFileExtension(originalFile.getName());
            Path nuevaRuta = imagenesDir.resolve(nuevoNombre);

            try {
                Files.copy(originalFile.toPath(), nuevaRuta, StandardCopyOption.REPLACE_EXISTING);
                Foto inserted = fotos.insert(muestreo.id(), parte, nuevaRuta.toString(), qcOk);

                resultado.append(qcOk ? "✅ " : "⚠️ ")
                        .append(originalFile.getName()).append(" → ").append(nuevoNombre).append("\n");
                resultado.append("   📋 ID BD: ").append(inserted.id()).append("\n");
                resultado.append("   🏢 Estación: ").append(muestreo.estacionNombre()).append(" (ID: ").append(muestreo.estacionId()).append(")\n");
                resultado.append("   🐟 Tanque: ").append(muestreo.tanqueCodigo()).append(" (ID: ").append(muestreo.tanqueId()).append(")\n");
                if (tipoPez != null && !tipoPez.trim().isEmpty()) {
                    resultado.append("   🐠 Especie: ").append(tipoPez).append("\n");
                }
                resultado.append("   📅 Fecha/Hora: ").append(muestreo.fechaHora()).append("\n");
                resultado.append("   🔍 Parte (registrada): ").append(parte.name()).append("\n");
                resultado.append("   💾 Ubicación física: ").append(nuevaRuta.toAbsolutePath()).append("\n");

                if (!qcOk) {
                    qcWarnings++;
                    resultado.append("   ⚠️ QC: ").append(qc.reason())
                            .append(" (Brillo=")
                            .append((int)qc.brightness()).append(", Foco=")
                            .append((int)qc.focus()).append(")\n");
                }
                resultado.append("   🤖 Clasificación: enviada automáticamente (AUTO)\n");
                resultado.append("\n");
                ok++;

                // Lanzar clasificación en segundo plano (modo AUTO)
                processor.processAsync(inserted, "AUTO", this::recargarTabla);
            } catch (IOException e) {
                resultado.append("❌ Error copiando ").append(originalFile.getName()).append(": ").append(e.getMessage()).append("\n");
                fail++;
            }
        }

        if (fail > 0) {
            resultado.append("❌ ").append(fail).append(" imágenes tuvieron errores.\n");
        }
        if (ok > 0) {
            resultado.append("✅ ").append(ok).append(" imágenes se guardaron correctamente en la carpeta 'imagenes'.");
        }
        if (qcWarnings > 0) {
            resultado.append("\n⚠️ ").append(qcWarnings).append(" imágenes requieren revisión de calidad.");
        }

        lblMsg.setText(resultado.toString());
        recargarTabla();
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    private MuestreoRow getOrCreateMuestreo() {
        Estacion estacion = cbEstacion.getValue();
        if (estacion == null) {
            var estaciones = estRepo.findAll();
            if (estaciones.isEmpty()) {
                throw new RuntimeException("Debes registrar al menos una estación antes de cargar fotos.");
            }
            estacion = estaciones.get(0);
            cbEstacion.getSelectionModel().select(estacion);
        }

        Tanque tanque = cbTanque.getValue();
        if (tanque == null) {
            var tanques = tanqRepo.findByEstacion(estacion.id());
            if (tanques.isEmpty()) {
                throw new RuntimeException("La estación seleccionada no tiene tanques registrados.");
            }
            tanque = tanques.get(0);
            cbTanque.getSelectionModel().select(tanque);
        }

        MuestreoRow existente = findLatestMuestreo(estacion.id(), tanque.id());
        if (existente != null) return existente;

        return createAutomaticMuestreo(estacion, tanque);
    }

    private MuestreoRow findLatestMuestreo(int estacionId, int tanqueId) {
        String sql = """
      SELECT m.ID, m.LOTE_ID, m.ESTACION_ID, e.NOMBRE AS ESTACION_NOMBRE,
             m.TANQUE_ID, t.CODIGO AS TANQUE_CODIGO, m.FECHA_HORA
      FROM MUESTREO m
      JOIN ESTACION e ON e.ID = m.ESTACION_ID
      JOIN TANQUE t   ON t.ID = m.TANQUE_ID
      WHERE m.ESTACION_ID=? AND m.TANQUE_ID=?
      ORDER BY m.ID DESC
      LIMIT 1
      """;
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, estacionId);
            ps.setInt(2, tanqueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MuestreoRow(
                            rs.getInt("ID"), rs.getInt("LOTE_ID"),
                            rs.getInt("ESTACION_ID"), rs.getString("ESTACION_NOMBRE"),
                            rs.getInt("TANQUE_ID"), rs.getString("TANQUE_CODIGO"),
                            rs.getString("FECHA_HORA")
                    );
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error consultando muestreos previos", e);
        }
    }

    private MuestreoRow createAutomaticMuestreo(Estacion estacion, Tanque tanque) {
        int loteId = ensureDefaultLote();
        muestreoRepo.insert(loteId, estacion.id(), tanque.id(), LocalDateTime.now());
        MuestreoRow creado = findLatestMuestreo(estacion.id(), tanque.id());
        if (creado == null) throw new RuntimeException("No fue posible registrar el muestreo automático");
        return creado;
    }

    private int ensureDefaultLote() {
        List<Lote> lotes = loteRepo.findAll();
        if (lotes != null && !lotes.isEmpty()) {
            return lotes.get(0).id();
        }
        Lote creado = loteRepo.insert("LOTE_AUTOMATICO");
        return creado.id();
    }

    private String getTipoPez(int tanqueId) {
        String sql = "SELECT TIPO_PEZ FROM TANQUE WHERE ID=?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tanqueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TIPO_PEZ");
                }
            }
        } catch (SQLException e) {
            // no crítico
        }
        return null;
    }

    // Diálogo rápido (enum del dominio) para registrar parte al subir imágenes
    private Parte askParte() {
        Dialog<Parte> d = new Dialog<>();
        d.setTitle("Parte");
        var ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        var opciones = FXCollections.<Parte>observableArrayList(Parte.values());
        opciones.sort(java.util.Comparator.comparingInt(p -> p == Parte.COMPLETO ? 0 : p.ordinal() + 1));
        var cb = new ComboBox<Parte>(opciones);
        cb.getSelectionModel().select(Parte.COMPLETO);
        GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("Parte:"), cb);
        d.getDialogPane().setContent(gp);
        d.setResultConverter(bt -> bt==ok? cb.getValue(): null);
        return d.showAndWait().orElse(null);
    }

    // ====== Botones de clasificación ======

    @FXML
    public void onClasificarSeleccionadas() {
        var seleccionadas = new ArrayList<>(tbl.getSelectionModel().getSelectedItems());
        if (seleccionadas.isEmpty()) {
            lblMsg.setText("Selecciona una o más filas.");
            return;
        }
        // Usar modo automático siempre (IA decide la parte)
        String parteElegida = "AUTO";
        int n = 0;
        for (Foto f : seleccionadas) {
            if (!f.qcOk() || f.estado()!=EstadoFoto.PENDIENTE) continue;
            processor.processAsync(f, parteElegida, this::recargarTabla); // <-- pasa "AUTO / OJO / BRANQUIAS"
            n++;
        }
        lblMsg.setText("Enviadas "+n+" a clasificación ("+parteElegida+").");
    }

    @FXML
    public void onClasificarPendientes() {
        var all = tbl.getItems();
        var pend = all.stream()
                .filter(f -> f.qcOk() && f.estado()==EstadoFoto.PENDIENTE)
                .collect(Collectors.toList());

        String parteElegida = "AUTO";

        for (Foto f : pend) {
            processor.processAsync(f, parteElegida, this::recargarTabla);
        }
        lblMsg.setText("Enviadas "+pend.size()+" a clasificación ("+parteElegida+").");
    }
}
