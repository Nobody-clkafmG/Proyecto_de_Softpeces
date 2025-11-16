package com.softpeces.ui;

import com.softpeces.audit.Audit;
import com.softpeces.catalog.Estacion;
import com.softpeces.catalog.EstacionRepository;
import com.softpeces.catalog.Tanque;
import com.softpeces.catalog.TanqueRepository;
import com.softpeces.domain.Lote;
import com.softpeces.domain.LoteEstado;
import com.softpeces.domain.MuestreoRow;
import com.softpeces.repo.LoteRepository;
import com.softpeces.repo.MuestreoRepository;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public class LotesMuestreosController {

    // Lotes
    @FXML private TableView<Lote> tblLotes;
    @FXML private TableColumn<Lote, Integer> colLoteId;
    @FXML private TableColumn<Lote, String> colLoteNombre;
    @FXML private TableColumn<Lote, String> colLoteEstado;
    @FXML private TextField txtFiltroLote;

    // Muestreos
    @FXML private TableView<MuestreoRow> tblMuestreos;
    @FXML private TableColumn<MuestreoRow, Integer> colMId;
    @FXML private TableColumn<MuestreoRow, String> colMEst;
    @FXML private TableColumn<MuestreoRow, String> colMTanq;
    @FXML private TableColumn<MuestreoRow, String> colMFecha;

    @FXML private Label lblMsg;

    private final LoteRepository lotes = new LoteRepository();
    private final MuestreoRepository muest = new MuestreoRepository();
    private final EstacionRepository estRepo = new EstacionRepository();
    private final TanqueRepository tanqRepo = new TanqueRepository();
    private FilteredList<Lote> dataLotes;

    @FXML
    public void initialize() {
        colLoteId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colLoteNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().nombre()));
        colLoteEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().estado().name()));
        recargarLotes();

        colMId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colMEst.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().estacionNombre()));
        colMTanq.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().tanqueCodigo()));
        colMFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().fechaHora()));

        tblLotes.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> recargarMuestreos());
    }

    private void recargarLotes() {
        dataLotes = new FilteredList<>(FXCollections.observableArrayList(lotes.findAll()));
        tblLotes.setItems(dataLotes);
        lblMsg.setText("");
        recargarMuestreos();
    }

    private void recargarMuestreos() {
        var lote = tblLotes.getSelectionModel().getSelectedItem();
        if (lote == null) { tblMuestreos.setItems(FXCollections.observableArrayList()); return; }
        tblMuestreos.setItems(FXCollections.observableArrayList(muest.findByLote(lote.id())));
    }

    // ---------- Lotes ----------
    @FXML public void onNuevoLote() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Nuevo lote"); d.setHeaderText(null); d.setContentText("Nombre:");
        d.showAndWait().ifPresent(n -> {
            try {
                var l = lotes.insert(n);
                Audit.log("Crear", "LOTE", l.id(), "nombre="+l.nombre());
                recargarLotes();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML public void onRenombrarLote() {
        var sel = tblLotes.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un lote."); return; }
        TextInputDialog d = new TextInputDialog(sel.nombre());
        d.setTitle("Renombrar lote"); d.setHeaderText(null); d.setContentText("Nombre:");
        d.showAndWait().ifPresent(n -> {
            try {
                lotes.rename(sel.id(), n);
                Audit.log("Editar", "LOTE", sel.id(), "nuevoNombre="+n);
                recargarLotes();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML public void onActivarLote() { cambiarEstado(LoteEstado.ACTIVO); }
    @FXML public void onCerrarLote()  { cambiarEstado(LoteEstado.CERRADO); }

    private void cambiarEstado(LoteEstado nuevo) {
        var sel = tblLotes.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un lote."); return; }
        try {
            lotes.changeState(sel.id(), nuevo);
            Audit.log("Estado", "LOTE", sel.id(), "-> "+nuevo.name());
            recargarLotes();
        } catch (Exception e) { lblMsg.setText(e.getMessage()); }
    }

    @FXML public void onEliminarLote() {
        var sel = tblLotes.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un lote."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar lote \""+sel.nombre()+"\"?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    lotes.delete(sel.id());
                    Audit.log("Eliminar", "LOTE", sel.id(), sel.nombre());
                    recargarLotes();
                } catch (Exception e) { lblMsg.setText(e.getMessage()); }
            }
        });
    }

    @FXML public void onFiltrarLote() {
        String f = txtFiltroLote.getText().toLowerCase().trim();
        Predicate<Lote> p = l -> f.isEmpty()
                || l.nombre().toLowerCase().contains(f)
                || l.estado().name().toLowerCase().contains(f);
        dataLotes.setPredicate(p);
    }

    // ---------- Muestreos ----------
    @FXML public void onNuevoMuestreo() {
        var lote = tblLotes.getSelectionModel().getSelectedItem();
        if (lote == null) { lblMsg.setText("Selecciona un lote."); return; }
        if (lote.estado() == LoteEstado.CERRADO) { lblMsg.setText("Lote cerrado: no admite muestreos."); return; }

        Dialog<Boolean> d = new Dialog<>();
        d.setTitle("Nuevo muestreo"); d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        var cbEst = new ComboBox<Estacion>(); cbEst.setPrefWidth(260);
        var cbTanq = new ComboBox<Tanque>();  cbTanq.setPrefWidth(200);

        List<Estacion> estaciones = estRepo.findAll();
        cbEst.setItems(FXCollections.observableArrayList(estaciones));
        cbEst.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Estacion e) { return e==null? "" : e.nombre(); }
            public Estacion fromString(String s) { return null; }
        });

        cbEst.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                var tanques = FXCollections.observableArrayList(tanqRepo.findByEstacion(b.id()));
                cbTanq.setItems(tanques);
                cbTanq.setConverter(new javafx.util.StringConverter<>() {
                    public String toString(Tanque t) { return t==null? "" : t.codigo(); }
                    public Tanque fromString(String s) { return null; }
                });
            } else cbTanq.setItems(FXCollections.observableArrayList());
        });

        GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("Estación:"), cbEst);
        gp.addRow(1, new Label("Tanque:"), cbTanq);
        d.getDialogPane().setContent(gp);

        d.setResultConverter(bt -> bt==ButtonType.OK);
        d.showAndWait().ifPresent(ok -> {
            try {
                var est = cbEst.getValue(); var t = cbTanq.getValue();
                if (est == null || t == null) throw new RuntimeException("Selecciona estación y tanque.");
                muest.insert(lote.id(), est.id(), t.id(), LocalDateTime.now());
                Audit.log("Crear", "MUESTREO", null, "lote="+lote.id()+" est="+est.id()+" tanq="+t.id());
                recargarMuestreos();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML public void onEliminarMuestreo() {
        var sel = tblMuestreos.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un muestreo."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar muestreo "+sel.id()+"?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    muest.delete(sel.id());
                    Audit.log("Eliminar", "MUESTREO", sel.id(), null);
                    recargarMuestreos();
                } catch (Exception e) { lblMsg.setText(e.getMessage()); }
            }
        });
    }
}
