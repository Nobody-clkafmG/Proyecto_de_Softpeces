package com.softpeces.ui;

import com.softpeces.audit.AuditRepository;
import com.softpeces.audit.AuditRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class BitacoraController {

    @FXML private TextField txtUser;                // sigue TextField
    @FXML private ComboBox<String> cbAccion;        // ahora ComboBox
    @FXML private ComboBox<String> cbEntidad;       // ahora ComboBox
    @FXML private DatePicker dpDesde, dpHasta;

    @FXML private TableView<AuditRow> tbl;
    @FXML private TableColumn<AuditRow,Integer> colId, colEntidadId;
    @FXML private TableColumn<AuditRow,String> colFecha, colUser, colAccion, colEntidad, colDetalle;
    @FXML private Label lblMsg;

    private final AuditRepository repo = new AuditRepository();

    @FXML
    public void initialize() {
        // columnas
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colEntidadId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().entidadId()));
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().fechaHora()));
        colUser.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().username()));
        colAccion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().accion()));
        colEntidad.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().entidad()));
        colDetalle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().detalle()));

        // opciones de filtros
        cbAccion.setItems(FXCollections.observableArrayList(
                "", "Crear", "Editar", "Eliminar", "Estado",
                "CargarFoto", "EnviarClasificacion", "Clasificar", "ClasificarError"
        ));
        cbAccion.getSelectionModel().selectFirst();

        cbEntidad.setItems(FXCollections.observableArrayList(
                "", "ESTACION", "TANQUE", "LOTE", "MUESTREO", "FOTO"
        ));
        cbEntidad.getSelectionModel().selectFirst();

        onBuscar();
    }

    @FXML
    public void onBuscar() {
        String user = txtUser.getText();
        String accion = normalize(cbAccion.getValue());
        String entidad = normalize(cbEntidad.getValue());

        var data = repo.search(user, accion, entidad, dpDesde.getValue(), dpHasta.getValue());
        tbl.setItems(FXCollections.observableArrayList(data));
        lblMsg.setText("Eventos: " + data.size());
    }

    private String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
