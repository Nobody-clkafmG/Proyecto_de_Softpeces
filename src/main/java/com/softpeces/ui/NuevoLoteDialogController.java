package com.softpeces.ui;

import com.softpeces.domain.Lote;
import com.softpeces.repo.LoteRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NuevoLoteDialogController {
    @FXML private TextField nombreField;
    @FXML private TextField departamentoField;
    @FXML private TextField municipioField;

    private final LoteRepository repo = new LoteRepository();
    private Lote creado;

    public Lote getResultado() {
        return creado;
    }

    @FXML public void onCrear() {
        var nombre = nombreField.getText();
        if (nombre == null || nombre.isBlank()) {
            mostrarError("El nombre del lote es obligatorio");
            return;
        }
        var dep = departamentoField.getText();
        var mun = municipioField.getText();
        try {
            creado = repo.insert(nombre, dep, mun);
            ((Stage) nombreField.getScene().getWindow()).close();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void mostrarError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg == null ? "Error desconocido" : msg).showAndWait();
    }
}