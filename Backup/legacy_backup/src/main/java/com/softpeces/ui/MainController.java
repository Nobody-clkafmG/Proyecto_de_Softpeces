package com.softpeces.ui;

import com.softpeces.auth.AuthService.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class MainController {
    @FXML private Label lblUser;
    private Session session;

    public void initSession(Session s) {
        this.session = s;
        lblUser.setText("Usuario: " + s.username + (s.isAdmin ? " (ADMIN)" : s.isOperador ? " (OPERADOR)" : ""));
    }

    @FXML
    public void onLogout() throws Exception {
        // Volver al login
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/ui/LoginView.fxml"));
        Scene scene = new Scene(fxml.load(), 420, 260);
        Stage stage = (Stage) lblUser.getScene().getWindow();
        stage.setTitle("Soft Peces — Iniciar sesión");
        stage.setScene(scene);
        stage.show();
    }
}
