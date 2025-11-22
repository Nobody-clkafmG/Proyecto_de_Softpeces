package com.softpeces.ui;

import com.softpeces.auth.PasswordResetService;
import com.softpeces.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ForgotPasswordController {
    @FXML private TextField usuarioField;
    @FXML private Label tokenLabel;
    @FXML private TextField tokenField;

    private final AuthService auth = new AuthService();
    private final PasswordResetService reset = new PasswordResetService();

    @FXML public void onGenerar() {
        try {
            String username = usuarioField.getText().trim();
            if (username.isEmpty()) {
                tokenLabel.setText("Por favor ingrese un nombre de usuario");
                return;
            }
            
            // Usar el método existente de AuthService
            var user = auth.findByUsername(username);
            if (user == null) { 
                tokenLabel.setText("Usuario no encontrado"); 
                return; 
            }
            
            if (!user.active()) {
                tokenLabel.setText("Usuario inactivo");
                return;
            }
            
            // Generar token de recuperación (30 minutos de validez)
            var token = reset.generarToken(user.id(), 30);
            tokenField.setText(token);
            tokenLabel.setText("Código generado. Copie y péguelo en la siguiente ventana.");
        } catch (Exception e) {
            e.printStackTrace();
            tokenLabel.setText("Error al procesar la solicitud");
        }
    }

    @FXML
    public void onAbrirReset() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ResetPasswordView.fxml"));
            Parent root = loader.load();

            Stage st = new Stage();
            st.setTitle("Cambiar contraseña");
            st.setScene(new Scene(root));
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de cambio de contraseña.\n" + e.getMessage())
                    .showAndWait();
        }
    }
}