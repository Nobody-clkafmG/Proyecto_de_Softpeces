package com.softpeces.ui;


import com.softpeces.domain.Lote;
import com.softpeces.domain.LoteEstado;
import com.softpeces.repo.LoteRepository;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class NuevoLoteDialogController {
    @FXML private TextField nombreField;
    @FXML private TextField departamentoField;
    @FXML private TextField municipioField;


    private final LoteRepository repo = new LoteRepository();


    @FXML public void onCrear() {
        var nombre = nombreField.getText();
        var dep = departamentoField.getText();
        var mun = municipioField.getText();
        if (nombre == null || nombre.isBlank()) return;
        var lote = new Lote(0, nombre, LoteEstado.ACTIVO, dep, mun);
        repo.insert(nombre); // ajusta según tu firma actual
        ((Stage) nombreField.getScene().getWindow()).close();
    }
}