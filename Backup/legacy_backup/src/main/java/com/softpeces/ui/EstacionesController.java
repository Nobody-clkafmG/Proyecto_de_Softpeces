package com.softpeces.ui;

import com.softpeces.audit.Audit;
import com.softpeces.catalog.Estacion;
import com.softpeces.catalog.EstacionRepository;
import com.softpeces.catalog.Tanque;
import com.softpeces.catalog.TanqueRepository;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.function.Predicate;

public class EstacionesController {

    // Estaciones
    @FXML private TableView<Estacion> table;
    @FXML private TableColumn<Estacion, Integer> colId;
    @FXML private TableColumn<Estacion, String> colNombre;
    @FXML private TextField txtFiltro;

    // Tanques
    @FXML private TableView<Tanque> tblTanques;
    @FXML private TableColumn<Tanque, Integer> colTId;
    @FXML private TableColumn<Tanque, String> colTCodigo;
    @FXML private TableColumn<Tanque, Double> colTCap;

    @FXML private Label lblMsg;

    private final EstacionRepository estRepo = new EstacionRepository();
    private final TanqueRepository tanqRepo = new TanqueRepository();

    private FilteredList<Estacion> dataEst;

    @FXML
    public void initialize() {
        // Estaciones
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().nombre()));
        recargarEstaciones();

        // Tanques
        colTId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colTCodigo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().codigo()));
        colTCap.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().capacidadL()));

        table.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> recargarTanques());
    }

    private void recargarEstaciones() {
        dataEst = new FilteredList<>(FXCollections.observableArrayList(estRepo.findAll()));
        table.setItems(dataEst);
        lblMsg.setText("");
        recargarTanques();
    }

    private void recargarTanques() {
        var sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            tblTanques.setItems(FXCollections.observableArrayList());
            return;
        }
        tblTanques.setItems(FXCollections.observableArrayList(tanqRepo.findByEstacion(sel.id())));
    }

    // -------- Estaciones --------
    @FXML public void onNueva() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Nueva Estación"); dlg.setHeaderText(null);
        dlg.setContentText("Nombre:");
        dlg.showAndWait().ifPresent(nombre -> {
            try {
                var e = estRepo.insert(nombre);
                Audit.log("Crear", "ESTACION", e.id(), "nombre="+e.nombre());
                recargarEstaciones();
            } catch (Exception ex) { lblMsg.setText(ex.getMessage()); }
        });
    }

    @FXML public void onEditar() {
        Estacion sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona una estación."); return; }
        TextInputDialog dlg = new TextInputDialog(sel.nombre());
        dlg.setTitle("Editar Estación"); dlg.setHeaderText(null);
        dlg.setContentText("Nombre:");
        dlg.showAndWait().ifPresent(nombre -> {
            try {
                estRepo.update(sel.id(), nombre);
                Audit.log("Editar", "ESTACION", sel.id(), "nuevoNombre="+nombre);
                recargarEstaciones();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML public void onEliminar() {
        Estacion sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona una estación."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar \""+sel.nombre()+"\"?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    estRepo.delete(sel.id());
                    Audit.log("Eliminar", "ESTACION", sel.id(), sel.nombre());
                    recargarEstaciones();
                } catch (Exception e) { lblMsg.setText(e.getMessage()); }
            }
        });
    }

    @FXML public void onFiltrar() {
        String f = txtFiltro.getText().toLowerCase().trim();
        Predicate<Estacion> p = e -> f.isEmpty() || e.nombre().toLowerCase().contains(f);
        dataEst.setPredicate(p);
    }

    // -------- Tanques --------
    @FXML public void onNuevoTanque() {
        Estacion est = table.getSelectionModel().getSelectedItem();
        if (est == null) { lblMsg.setText("Selecciona una estación para crear tanques."); return; }

        Dialog<Tanque> dlg = dlgTanque(null);
        dlg.showAndWait().ifPresent(t -> {
            try {
                var nt = tanqRepo.insert(est.id(), t.codigo(), t.capacidadL());
                Audit.log("Crear", "TANQUE", nt.id(), "est="+est.id()+" cod="+nt.codigo()+" cap="+nt.capacidadL());
                recargarTanques();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML public void onEditarTanque() {
        Tanque sel = tblTanques.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un tanque."); return; }

        Dialog<Tanque> dlg = dlgTanque(sel);
        dlg.showAndWait().ifPresent(t -> {
            try {
                tanqRepo.update(sel.id(), t.codigo(), t.capacidadL());
                Audit.log("Editar", "TANQUE", sel.id(), "cod="+t.codigo()+" cap="+t.capacidadL());
                recargarTanques();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML public void onEliminarTanque() {
        Tanque sel = tblTanques.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un tanque."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar tanque \""+sel.codigo()+"\"?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    tanqRepo.delete(sel.id());
                    Audit.log("Eliminar", "TANQUE", sel.id(), sel.codigo());
                    recargarTanques();
                } catch (Exception e) { lblMsg.setText(e.getMessage()); }
            }
        });
    }

    private Dialog<Tanque> dlgTanque(Tanque base) {
        Dialog<Tanque> d = new Dialog<>();
        d.setTitle(base == null ? "Nuevo tanque" : "Editar tanque");
        var ok = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField tfCodigo = new TextField(base == null ? "" : base.codigo());
        TextField tfCap = new TextField(base == null ? "" : String.valueOf(base.capacidadL()));
        GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("Código:"), tfCodigo);
        gp.addRow(1, new Label("Capacidad (L):"), tfCap);
        d.getDialogPane().setContent(gp);

        d.setResultConverter(bt -> {
            if (bt == ok) {
                String cod = tfCodigo.getText().trim();
                String capStr = tfCap.getText().trim();
                if (cod.isEmpty()) throw new RuntimeException("El código no puede estar vacío");
                double cap;
                try { cap = Double.parseDouble(capStr); }
                catch (NumberFormatException ex) { throw new RuntimeException("Capacidad inválida (usa números)"); }
                if (cap <= 0) throw new RuntimeException("La capacidad debe ser > 0");
                return new Tanque(-1, -1, cod, cap);
            }
            return null;
        });
        return d;
    }
}
