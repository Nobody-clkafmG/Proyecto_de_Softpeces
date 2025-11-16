package com.softpeces.ui;


import com.softpeces.auth.PasswordResetService;
import com.softpeces.auth.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class ForgotPasswordController {
    @FXML private TextField usuarioField;
    @FXML private Label tokenLabel;


    private final AuthService auth = new AuthService();
    private final PasswordResetService reset = new PasswordResetService();


    @FXML public void onGenerar() {
        var user = auth.findByUsername(usuarioField.getText()); // añade este método si no existe
        if (user == null) { tokenLabel.setText("Usuario no encontrado"); return; }
        var token = reset.generarToken(user.id(), 30); // 30 min
        tokenLabel.setText("Tu código: " + token);
    }
}