package com.softpeces.ui;

import com.softpeces.domain.Inspeccion;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;

public class InspeccionDialogController {

    @FXML private DialogPane dialogPane;
    @FXML private Slider slOlor, slTextura, slColor;
    @FXML private TextArea txComentarios;

    private Inspeccion result; // solo valores; muestreoId se asigna afuera

    @FXML
    private void initialize() {
        // “force” enteros 1..5
        slOlor.valueProperty().addListener((obs, a, b) -> slOlor.setValue(Math.round(b.doubleValue())));
        slTextura.valueProperty().addListener((obs, a, b) -> slTextura.setValue(Math.round(b.doubleValue())));
        slColor.valueProperty().addListener((obs, a, b) -> slColor.setValue(Math.round(b.doubleValue())));

        dialogPane.lookupButton(dialogPane.getButtonTypes().get(1)) // "Guardar"
                .addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
                    // validaciones simples (opcional)
                    // nada que valide impide cerrar, pero podrías bloquear si quisieras
                    result = new Inspeccion(
                            null,
                            -1, // muestreoId lo setea el caller
                            (int) slOlor.getValue(),
                            (int) slTextura.getValue(),
                            (int) slColor.getValue(),
                            txComentarios.getText(),
                            null
                    );
                });
    }

    public Inspeccion getResult() {
        return result;
    }
}
