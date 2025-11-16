package com.softpeces.ui;

import com.softpeces.model.Limpieza;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class LimpiezaDialogController {
    @FXML private DatePicker dpFecha;
    @FXML private TextField  txtResp;
    @FXML private TextArea   txtDesc;

    private int tanqueId;
    private Limpieza result;
    private Runnable onSaved;

    public void setTanqueId(int id) { this.tanqueId = id; }
    public Limpieza getResult() { return result; }
    public void setOnSaved(Runnable r) { this.onSaved = r; }

    @FXML
    public void initialize() {
        dpFecha.setValue(LocalDate.now());
    }

    @FXML
    public void onSave() {
        result = new Limpieza(
                null,
                tanqueId,
                dpFecha.getValue(),
                txtResp.getText(),
                txtDesc.getText()
        );
        if (onSaved != null) onSaved.run();
        close();
    }

    @FXML
    public void onCancel() {
        result = null;
        close();
    }

    private void close() {
        ((Stage) dpFecha.getScene().getWindow()).close();
    }
}
