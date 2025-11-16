package com.softpeces.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class ParteDialogController {
    @FXML private ComboBox<String> cmbParte;

    private String result = null;

    @FXML
    public void initialize() {
        // Orden recomendado: AUTO, OJO, BRANQUIAS
        cmbParte.getItems().setAll("AUTO", "OJO", "BRANQUIAS");
        cmbParte.getSelectionModel().selectFirst();
    }

    public void setInitial(String parte) {
        if (parte == null || parte.isBlank()) return;
        for (String s : cmbParte.getItems()) {
            if (s.equalsIgnoreCase(parte)) {
                cmbParte.getSelectionModel().select(s);
                break;
            }
        }
    }

    public String getResult() { return result; }

    @FXML
    public void onOk() {
        result = cmbParte.getValue();
        close();
    }

    @FXML
    public void onCancel() {
        result = null;
        close();
    }

    private void close() {
        Stage st = (Stage) cmbParte.getScene().getWindow();
        st.close();
    }
}
