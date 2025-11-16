package com.softpeces.ui;

import com.softpeces.catalog.*;
import com.softpeces.domain.*;
import com.softpeces.qc.ImageQuality;
import com.softpeces.repo.FotoRepository;
import com.softpeces.repo.MuestreoRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.scene.control.SelectionMode;


import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FotosController {

    @FXML private ComboBox<Lote> cbLote;
    @FXML private ComboBox<MuestreoRow> cbMuestreo;
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

    private final com.softpeces.repo.LoteRepository lotes = new com.softpeces.repo.LoteRepository();
    private final MuestreoRepository muest = new MuestreoRepository();
    private final FotoRepository fotos = new FotoRepository();
    private final EstacionRepository estRepo = new EstacionRepository();
    private final TanqueRepository tanqRepo = new TanqueRepository();
    private final com.softpeces.ia.FotoProcessor processor = new com.softpeces.ia.FotoProcessor();

    @FXML
    public void initialize() {
        // Lotes combo
        var dataLotes = FXCollections.observableArrayList(lotes.findAll());
        cbLote.setItems(dataLotes);
        cbLote.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Lote l) { return l==null? "" : (l.id()+" - "+l.nombre()+" ("+l.estado()+")"); }
            public Lote fromString(String s) { return null; }
        });
        cbLote.getSelectionModel().selectedItemProperty().addListener((o,a,b)-> loadMuestreos());

        // Muestreos combo
        cbMuestreo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(MuestreoRow m) { return m==null? "" : (m.id()+" — "+m.estacionNombre()+" / "+m.tanqueCodigo()+" @ "+m.fechaHora()); }
            public MuestreoRow fromString(String s) { return null; }
        });
        cbMuestreo.getSelectionModel().selectedItemProperty().addListener((o,a,b)-> recargarTabla());

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
    }

    private void loadMuestreos() {
        var l = cbLote.getValue();
        if (l==null) { cbMuestreo.setItems(FXCollections.observableArrayList()); recargarTabla(); return; }
        cbMuestreo.setItems(FXCollections.observableArrayList(muest.findByLote(l.id())));
        recargarTabla();
    }

    private void recargarTabla() {
        var m = cbMuestreo.getValue();
        if (m==null) { tbl.setItems(FXCollections.observableArrayList()); return; }
        tbl.setItems(FXCollections.observableArrayList(fotos.findByMuestreo(m.id())));
    }

    @FXML
    public void onAgregarFotos() {
        var m = cbMuestreo.getValue();
        if (m==null) { lblMsg.setText("Selecciona un muestreo."); return; }

        // Diálogo para seleccionar PARTE
        Parte parte = askParte();
        if (parte == null) return;

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg","*.jpeg","*.png","*.bmp")
        );
        List<File> files = fc.showOpenMultipleDialog(tbl.getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        int ok=0, fail=0;
        for (File f : files) {
            var qc = ImageQuality.check(f.getAbsolutePath());
            boolean pass = qc.ok();
            var inserted = fotos.insert(m.id(), parte, f.getAbsolutePath(), pass);
            if (!pass) {
                // marcamos mensaje de error
                fotos.updateEstado(inserted.id(), EstadoFoto.ERROR, null, null, qc.reason()+" (Brillo="+(int)qc.brightness()+", Foco="+(int)qc.focus()+")");
                fail++;
            } else ok++;
        }
        lblMsg.setText("Cargadas: "+ok+" OK, "+fail+" fallidas (QC).");
        recargarTabla();
    }

    private Parte askParte() {
        Dialog<Parte> d = new Dialog<>();
        d.setTitle("Parte");
        var ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        var cb = new ComboBox<Parte>(FXCollections.observableArrayList(Parte.values()));
        cb.getSelectionModel().selectFirst();
        GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("Parte:"), cb);
        d.getDialogPane().setContent(gp);
        d.setResultConverter(bt -> bt==ok? cb.getValue(): null);
        return d.showAndWait().orElse(null);
    }

    @FXML
    public void onClasificarSeleccionadas() {
        var sel = tbl.getSelectionModel().getSelectedItems();
        if (sel==null || sel.isEmpty()) { lblMsg.setText("Selecciona una o más filas."); return; }
        int n = 0;
        for (Foto f : sel) {
            if (!f.qcOk() || f.estado()!=EstadoFoto.PENDIENTE) continue;
            processor.processAsync(f, this::recargarTabla);
            n++;
        }
        lblMsg.setText("Enviadas "+n+" a clasificación.");
    }

    @FXML
    public void onClasificarPendientes() {
        var all = tbl.getItems();
        var pend = all.stream().filter(f -> f.qcOk() && f.estado()==EstadoFoto.PENDIENTE).collect(Collectors.toList());
        for (Foto f : pend) processor.processAsync(f, this::recargarTabla);
        lblMsg.setText("Enviadas "+pend.size()+" a clasificación.");
    }
}
