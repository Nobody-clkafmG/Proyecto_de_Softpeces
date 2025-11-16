package com.softpeces.ui;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public class NuevaEstacionDialogController {

    @FXML private TextField txtSitio, txtEncargado, txtGeo;
    @FXML private Spinner<Integer> spnTanques;
    @FXML private Label lblError;
    @FXML private Label lblCantTanques;

    private boolean modoEdicion = false;
    private int tanquesOriginal = 1;

    // Métodos de acceso para los campos de texto
    public TextField getTxtSitio() {
        return txtSitio;
    }
    
    public TextField getTxtEncargado() {
        return txtEncargado;
    }
    
    public TextField getTxtGeo() {
        return txtGeo;
    }
    
    public Spinner<Integer> getSpnTanques() {
        return spnTanques;
    }

    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
        if (modoEdicion) {
            tanquesOriginal = spnTanques.getValue();
            ocultarSpinner(true);
        } else {
            ocultarSpinner(false);
        }
    }

    @FXML
    public void initialize() {
        spnTanques.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        lblError.setText("");
        ocultarSpinner(false);
    }

    public Optional<NuevaEstacionRequest> buildRequest() {
        String sitio     = txtSitio.getText()     == null ? "" : txtSitio.getText().trim();
        String encargado = txtEncargado.getText() == null ? "" : txtEncargado.getText().trim();
        String geo       = txtGeo.getText()       == null ? "" : txtGeo.getText().trim();

        if (sitio.isBlank())         { lblError.setText("El nombre del sitio es obligatorio."); return Optional.empty(); }
        if (encargado.isBlank())     { lblError.setText("El nombre del encargado es obligatorio."); return Optional.empty(); }

        int cantTanques = modoEdicion ? tanquesOriginal : spnTanques.getValue();

        return Optional.of(new NuevaEstacionRequest(
                sitio, encargado, geo,
                cantTanques
        ));
    }

    private void ocultarSpinner(boolean ocultar) {
        spnTanques.setVisible(!ocultar);
        spnTanques.setManaged(!ocultar);
        if (lblCantTanques != null) {
            lblCantTanques.setVisible(!ocultar);
            lblCantTanques.setManaged(!ocultar);
        }
    }
}
