package com.softpeces.ui;

import com.softpeces.auth.PasswordResetService;
import com.softpeces.auth.UserRepository;
import com.softpeces.infra.MailService;
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
    @FXML private TextField correoField;
    @FXML private Label tokenLabel;

    private final UserRepository users = new UserRepository();
    private final PasswordResetService reset = new PasswordResetService();
    private final MailService mail = new MailService();

    @FXML public void onGenerar() {
        try {
            String username = usuarioField.getText().trim();
            String correo = correoField.getText().trim();

            if (username.isEmpty()) {
                tokenLabel.setText("Por favor ingrese un nombre de usuario");
                return;
            }
            if (correo.isEmpty()) {
                tokenLabel.setText("Por favor ingrese el correo registrado");
                return;
            }
            
            var user = users.findByUsername(username);
            if (user == null) { 
                tokenLabel.setText("Usuario no encontrado"); 
                return; 
            }
            
            if (!user.active()) {
                tokenLabel.setText("Usuario inactivo");
                return;
            }

            if (user.email() == null || user.email().isBlank()) {
                tokenLabel.setText("El usuario no tiene un correo registrado");
                return;
            }

            if (!user.email().equalsIgnoreCase(correo)) {
                tokenLabel.setText("El correo no coincide con el registrado para el usuario");
                return;
            }
            
            // Generar token de recuperación (30 minutos de validez)
            var token = reset.generarToken(user.id(), 30);

            boolean enviado = mail.send(
                    user.email(),
                    "Recuperación de contraseña",
                    "Hola %s,%n%nHemos recibido una solicitud para restablecer tu contraseña.".formatted(user.username()) +
                            "\n\nCódigo de recuperación: " + token +
                            "\n\nEste código expira en 30 minutos." +
                            "\n\nSi no solicitaste este cambio, ignora este mensaje." +
                            "\n\nSoft Peces"
            );

            if (enviado) {
                tokenLabel.setText("Se envió un correo con las instrucciones para recuperar la contraseña.");
            } else {
                tokenLabel.setText("No se pudo enviar el correo. Verifique la configuración o contacte al administrador.");
            }
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