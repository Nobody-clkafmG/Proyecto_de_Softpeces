package com.softpeces.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import com.softpeces.arch.application.RegistrarEstacionYTanquesService;
import com.softpeces.audit.Audit;
import com.softpeces.catalog.Estacion;
import com.softpeces.catalog.EstacionRepository;
import com.softpeces.catalog.Tanque;
import com.softpeces.catalog.TanqueRepository;
import com.softpeces.model.Limpieza;
import com.softpeces.repo.LimpiezaRepository;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EstacionesController {

    // Root de la vista
    @FXML private BorderPane root;

    // Botón "Nueva"
    @FXML private Button btnNueva;

    // Estaciones
    @FXML private TableView<Estacion> table;
    @FXML private TableColumn<Estacion, Integer> colId;
    @FXML private TableColumn<Estacion, String>  colNombre;
    @FXML private TableColumn<Estacion, String>  colEncargado;
    @FXML private TableColumn<Estacion, String>  colGeo;
    @FXML private TableColumn<Estacion, Integer> colCantTanques;
    @FXML private TextField txtFiltro;

    // Tanques
    @FXML private TableView<Tanque> tblTanques;
    @FXML private TableColumn<Tanque, Integer> colTId;
    @FXML private TableColumn<Tanque, String>  colTCodigo;
    @FXML private TableColumn<Tanque, Double>  colTCap;
    @FXML private TableColumn<Tanque, String>  colTTipoPez;
    @FXML private TableColumn<Tanque, Integer> colTPeces;
    @FXML private TableColumn<Tanque, String>  colTFecha;

    // Historial de limpiezas
    @FXML private TableView<Limpieza> limpiezasTable;
    @FXML private TableColumn<Limpieza, LocalDate> colLimpFecha;
    @FXML private TableColumn<Limpieza, String>    colLimpResp;
    @FXML private TableColumn<Limpieza, String>    colLimpDesc;

    @FXML private Label lblMsg;

    private final LimpiezaRepository limpiezaRepo = new LimpiezaRepository();
    private Integer tanqueSelectedId;

    private final EstacionRepository estRepo = new EstacionRepository();
    private final TanqueRepository   tanqRepo = new TanqueRepository();

    private FilteredList<Estacion> dataEst;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // Estaciones
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colEncargado.setCellValueFactory(c -> new SimpleStringProperty(valorSeguro(c.getValue().encargado())));
        colGeo.setCellValueFactory(c -> new SimpleStringProperty(valorSeguro(c.getValue().geoUbicacion())));
        colCantTanques.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().cantidadTanques()));

        // Tanques
        colTId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colTCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigo()));
        colTCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().capacidadL()));
        colTTipoPez.setCellValueFactory(c -> new SimpleStringProperty(valorSeguro(c.getValue().tipoPez())));
        colTPeces.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().pecesAprox()));
        colTFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().fechaInicio() == null ? "" : DATE_FMT.format(c.getValue().fechaInicio())));

        // Limpiezas
        colLimpFecha.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getFecha()));
        colLimpResp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getResponsable()));
        colLimpDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescripcion()));

        // Listeners
        table.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> recargarTanques());

        tblTanques.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> {
            tanqueSelectedId = (b == null) ? null : b.id(); // si Tanque es record
            refrescarLimpiezas();
        });

        // Carga inicial
        recargarEstaciones();
    }

    private void refrescarLimpiezas() {
        var tanque = tblTanques.getSelectionModel().getSelectedItem();
        if (tanque == null) {
            limpiezasTable.getItems().clear();
            return;
        }
        var rows = limpiezaRepo.findByTanque(tanque.id()); // si Tanque es record; si no, usa getId()
        limpiezasTable.getItems().setAll(rows);
    }

    @FXML
    public void onAgregarLimpieza() {
        var tanque = tblTanques.getSelectionModel().getSelectedItem();
        if (tanque == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/LimpiezaDialog.fxml"));
            Parent root = loader.load();

            LimpiezaDialogController ctrl = loader.getController();
            ctrl.setTanqueId(tanque.id());           // si no es record, usa tanque.getId()
            ctrl.setOnSaved(this::refrescarLimpiezas);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Registrar limpieza");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            var l = ctrl.getResult();
            if (l != null) {
                limpiezaRepo.insert(l);
                refrescarLimpiezas();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onEliminarLimpieza() {
        var sel = limpiezasTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        limpiezaRepo.delete(sel.getId());
        refrescarLimpiezas();
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
            limpiezasTable.getItems().clear();
            return;
        }
        tblTanques.setItems(FXCollections.observableArrayList(tanqRepo.findByEstacion(sel.id())));
        refrescarLimpiezas();
    }

    private String valorSeguro(String v) {
        return v == null ? "" : v;
    }

    // -------- Estaciones --------

    @FXML
    private void onNuevaEstacion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/NuevaEstacionDialog.fxml"));
            DialogPane pane = loader.load();
            NuevaEstacionDialogController ctrl = loader.getController();

            Dialog<NuevaEstacionRequest> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Nueva estación / estanques");

            ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().setAll(btnOk, btnCancel);

            Button btnSave = (Button) dialog.getDialogPane().lookupButton(btnOk);
            btnSave.setDisable(true);

            ctrl.getTxtSitio().textProperty().addListener((obs, oldVal, newVal) -> {
                btnSave.setDisable(newVal.trim().isEmpty() ||
                        ctrl.getTxtEncargado().getText().trim().isEmpty());
            });

            ctrl.getTxtEncargado().textProperty().addListener((obs, oldVal, newVal) -> {
                btnSave.setDisable(newVal.trim().isEmpty() ||
                        ctrl.getTxtSitio().getText().trim().isEmpty());
            });

            dialog.setResultConverter(btn -> {
                if (btn == btnOk) {
                    return ctrl.buildRequest().orElse(null);
                }
                return null;
            });

            dialog.showAndWait().ifPresent(req -> {
                try {
                    RegistrarEstacionYTanquesService service = new RegistrarEstacionYTanquesService();
                    int idEstacion = service.crear(req);

                    if (idEstacion > 0) {
                        recargarEstaciones();
                        lblMsg.setText("Estación guardada (ID: " + idEstacion + ")");

                        table.getItems().stream()
                                .filter(e -> e.id() == idEstacion)
                                .findFirst()
                                .ifPresent(estacion -> {
                                    table.getSelectionModel().select(estacion);
                                    table.scrollTo(estacion);
                                });
                    } else {
                        lblMsg.setText("No se pudo guardar la estación");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    lblMsg.setText("Error al guardar la estación: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Error al abrir el diálogo: " + e.getMessage());
        }
    }

    @FXML
    public void onEditar() {
        Estacion sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona una estación."); return; }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/NuevaEstacionDialog.fxml"));
            DialogPane dialogPane = loader.load();

            NuevaEstacionDialogController ctrl = loader.getController();
            ctrl.getTxtSitio().setText(sel.nombre());
            ctrl.getTxtEncargado().setText(sel.encargado());
            ctrl.getTxtGeo().setText(sel.geoUbicacion());
            ctrl.getSpnTanques().getValueFactory().setValue(sel.cantidadTanques() == null ? 1 : sel.cantidadTanques());
            ctrl.setModoEdicion(true);

            Dialog<NuevaEstacionRequest> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Editar Estación");

            ButtonType okType = dialogPane.getButtonTypes().stream()
                    .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    .findFirst()
                    .orElse(null);
            if (okType != null) {
                Button btnSave = (Button) dialogPane.lookupButton(okType);
                if (btnSave != null) btnSave.setDisable(false);
            }

            dialog.setResultConverter(btn -> {
                if (okType != null && btn == okType) return ctrl.buildRequest().orElse(null);
                return null;
            });

            dialog.showAndWait().ifPresent(req -> {
                try {
                    estRepo.update(sel.id(), req.sitio(), req.encargado(), req.geo(), req.cantidadTanques());
                    Audit.log("Editar", "ESTACION", sel.id(),
                            "sitio=" + req.sitio() + " encargado=" + req.encargado()
                                    + " geo=" + req.geo() + " cantidadTanques=" + req.cantidadTanques());
                    recargarEstaciones();
                    lblMsg.setText("Estación actualizada");
                } catch (Exception e) {
                    lblMsg.setText("Error al actualizar: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            lblMsg.setText("Error al abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminar() {
        Estacion sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona una estación."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + sel.nombre() + "\"?", ButtonType.OK, ButtonType.CANCEL);
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

    @FXML
    public void onFiltrar() {
        String f = txtFiltro.getText().toLowerCase().trim();
        Predicate<Estacion> p = e -> f.isEmpty() || e.nombre().toLowerCase().contains(f);
        dataEst.setPredicate(p);
    }

    // -------- Tanques --------

    @FXML
    public void onNuevoTanque() {
        Estacion est = table.getSelectionModel().getSelectedItem();
        if (est == null) { lblMsg.setText("Selecciona una estación para crear tanques."); return; }

        Dialog<Tanque> dlg = dlgTanque(null);
        dlg.showAndWait().ifPresent(t -> {
            try {
                var nt = tanqRepo.insert(est.id(), t.codigo(), t.capacidadL(), t.tipoPez(), t.pecesAprox(), t.fechaInicio());
                Audit.log("Crear", "TANQUE", nt.id(), "est=" + est.id() + " cod=" + nt.codigo() + " cap=" + nt.capacidadL());
                estRepo.actualizarCantidadTanques(est.id());
                recargarTanques();
                recargarEstaciones();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML
    public void onEditarTanque() {
        Tanque sel = tblTanques.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un tanque."); return; }

        Dialog<Tanque> dlg = dlgTanque(sel);
        dlg.showAndWait().ifPresent(t -> {
            try {
                tanqRepo.update(sel.id(), t.codigo(), t.capacidadL(), t.tipoPez(), t.pecesAprox(), t.fechaInicio());
                Audit.log("Editar", "TANQUE", sel.id(), "cod=" + t.codigo() + " cap=" + t.capacidadL());
                estRepo.actualizarCantidadTanques(sel.estacionId());
                recargarTanques();
                recargarEstaciones();
            } catch (Exception e) { lblMsg.setText(e.getMessage()); }
        });
    }

    @FXML
    public void onEliminarTanque() {
        Tanque sel = tblTanques.getSelectionModel().getSelectedItem();
        if (sel == null) { lblMsg.setText("Selecciona un tanque."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar tanque \"" + sel.codigo() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    tanqRepo.delete(sel.id());
                    Audit.log("Eliminar", "TANQUE", sel.id(), sel.codigo());
                    estRepo.actualizarCantidadTanques(sel.estacionId());
                    recargarTanques();
                    recargarEstaciones();
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
        TextField tfCap    = new TextField(base == null ? "" : String.valueOf(base.capacidadL()));
        ComboBox<String> cbTipoPez = new ComboBox<>(FXCollections.observableArrayList(com.softpeces.catalog.TipoPez.TODOS));
        String tipoPezInicial = (base == null || base.tipoPez() == null || base.tipoPez().isEmpty()) ? null : base.tipoPez();
        cbTipoPez.setValue(tipoPezInicial);
        if (cbTipoPez.getValue() == null && !cbTipoPez.getItems().isEmpty()) {
            cbTipoPez.setValue(cbTipoPez.getItems().get(0));
        }
        TextField tfPeces = new TextField(base == null ? "" : (base.pecesAprox() == null ? "" : String.valueOf(base.pecesAprox())));
        DatePicker dpFecha = new DatePicker();
        if (base != null && base.fechaInicio() != null) dpFecha.setValue(base.fechaInicio());
        else dpFecha.setValue(LocalDate.now());

        GridPane gp = new GridPane(); gp.setHgap(8); gp.setVgap(8);
        gp.addRow(0, new Label("Código:"),        tfCodigo);
        gp.addRow(1, new Label("Capacidad (L):"), tfCap);
        gp.addRow(2, new Label("Tipo de pez:"),   cbTipoPez);
        gp.addRow(3, new Label("Peces aprox.:"),  tfPeces);
        gp.addRow(4, new Label("Fecha inicio:"),  dpFecha);
        d.getDialogPane().setContent(gp);

        d.setResultConverter(bt -> {
            if (bt == ok) {
                String cod     = tfCodigo.getText().trim();
                String capStr  = tfCap.getText().trim();
                String tipoPez = cbTipoPez.getValue();
                if (tipoPez == null) tipoPez = "";
                String pecesStr = tfPeces.getText().trim();

                if (cod.isEmpty()) throw new RuntimeException("El código no puede estar vacío");

                double cap;
                try { cap = Double.parseDouble(capStr); }
                catch (NumberFormatException ex) { throw new RuntimeException("Capacidad inválida (usa números)"); }
                if (cap <= 0) throw new RuntimeException("La capacidad debe ser > 0");

                Integer peces = null;
                if (!pecesStr.isEmpty()) {
                    try { peces = Integer.parseInt(pecesStr); }
                    catch (NumberFormatException ex) { throw new RuntimeException("Peces aprox. inválido (usa números)"); }
                    if (peces < 0) throw new RuntimeException("Los peces aprox. deben ser >= 0");
                }

                LocalDate fecha = dpFecha.getValue();
                if (fecha == null) fecha = LocalDate.now();

                return new Tanque(-1, -1, cod, cap, tipoPez, peces, fecha);
            }
            return null;
        });
        return d;
    }
}
