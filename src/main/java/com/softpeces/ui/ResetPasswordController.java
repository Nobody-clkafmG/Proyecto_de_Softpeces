package com.softpeces.ui;


import com.softpeces.auth.AuthService;
import com.softpeces.auth.PasswordResetService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class ResetPasswordController {
    @FXML private TextField tokenField;
    @FXML private PasswordField nuevaPassField;
    @FXML private Label estadoLabel;


    private final AuthService auth = new AuthService();
    private final PasswordResetService reset = new PasswordResetService();


    @FXML public void onCambiar() {
        var token = tokenField.getText();
        var userId = reset.validarToken(token);
        if (userId == null) { estadoLabel.setText("Token inválido o expirado"); return; }
        auth.updatePassword(userId, nuevaPassField.getText()); // añade este método si no existe
        reset.consumirToken(token);
        estadoLabel.setText("Contraseña actualizada");
    }
}