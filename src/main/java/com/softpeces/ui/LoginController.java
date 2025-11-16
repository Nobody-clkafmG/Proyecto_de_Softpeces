package com.softpeces.ui;

import com.softpeces.auth.AuthContext;
import com.softpeces.auth.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblMsg;
    @FXML private CheckBox chkRecordar;

    @FXML
    private void login() {
        String u = txtUser.getText() == null ? "" : txtUser.getText().trim();
        String p = txtPass.getText() == null ? "" : txtPass.getText().trim();

        lblMsg.setText("");

        if (u.isBlank() || p.isBlank()) {
            lblMsg.setText("Ingresa usuario y contraseña.");
            return;
        }

        try {
            AuthService auth = new AuthService();
            AuthService.Session session = auth.login(u, p);
            AuthContext.set(session);

            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
            Scene scene = new Scene(fxml.load(), 1280, 800);
            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setTitle("Soft Peces");
            stage.setScene(scene);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            lblMsg.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void onForgot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ForgotPasswordView.fxml"));
            Parent root = loader.load();

            Stage st = new Stage();
            st.setTitle("Recuperar contraseña");
            st.setScene(new Scene(root));
            st.initModality(Modality.APPLICATION_MODAL);
            st.showAndWait();
        } catch (IOException e) {
            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "No se pudo abrir la ventana de recuperación.\n" + e.getMessage()
            ).showAndWait();
        }
    }
}
