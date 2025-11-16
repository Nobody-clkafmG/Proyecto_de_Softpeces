package com.softpeces.ui;

import com.softpeces.auth.AuthContext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane content;
    @FXML private Button btnEstaciones, btnLotes, btnFotos, btnBitacora, btnReportes, btnUsuarios;
    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarMail;
    
    @FXML
    public void initialize() {
        // Cargar por defecto “Estaciones y Tanques”
        showEstaciones();

        var s = com.softpeces.auth.AuthContext.get();
    String username = (s != null && s.username != null && !s.username.isBlank())
            ? s.username
            : "Usuario";
    lblSidebarName.setText(username);
    lblSidebarMail.setText(username + "@mail.com");
        boolean admin = com.softpeces.auth.AuthContext.isAdmin();
        btnUsuarios.setVisible(admin);
        btnUsuarios.setManaged(admin);
    }

    // ---------- Navegación ----------
    @FXML
    public void showEstaciones() { loadView("/ui/EstacionesTanquesTab.fxml"); setActive(btnEstaciones); }

    @FXML
    public void showLotes() { loadView("/ui/LotesMuestreosTab.fxml"); setActive(btnLotes); }

    @FXML
    public void showFotos() { loadView("/ui/FotosTab.fxml"); setActive(btnFotos); }

    @FXML
    public void showBitacora() { loadView("/ui/BitacoraTab.fxml"); setActive(btnBitacora); }

    @FXML
    public void showReportes() { loadView("/ui/ReportesTab.fxml"); setActive(btnReportes); }

    @FXML
    public void showUsuarios() {
        if (!com.softpeces.auth.AuthContext.isAdmin()) {
            return;
        }
        loadView("/ui/UsuariosTab.fxml");
        setActive(btnUsuarios);
    }

    private void loadView(String resource) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(resource));
            content.getChildren().setAll(view);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar: " + resource, e);
        }
    }

    private void setActive(Button active) {
        btnEstaciones.getStyleClass().remove("selected");
        btnLotes.getStyleClass().remove("selected");
        btnFotos.getStyleClass().remove("selected");
        btnBitacora.getStyleClass().remove("selected");
        btnReportes.getStyleClass().remove("selected");
        if (btnUsuarios != null) btnUsuarios.getStyleClass().remove("selected");
        if (!active.getStyleClass().contains("selected")) {
            active.getStyleClass().add("selected");
        }
    }

    // ---------- Cerrar sesión ----------
    @FXML
    public void logout() {
        try {
            AuthContext.set(null); // limpia sesión
            var loader = new FXMLLoader(getClass().getResource("/ui/LoginView.fxml"));
            var scene  = new Scene(loader.load(), 560, 420); // tamaño cómodo de login
            Stage stage = (Stage) content.getScene().getWindow();
            stage.setTitle("Soft Peces — Iniciar sesión");
            stage.setScene(scene);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo volver al login", e);
        }
    }
}
