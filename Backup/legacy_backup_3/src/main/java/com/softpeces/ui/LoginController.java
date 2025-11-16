package com.softpeces.ui;

import com.softpeces.auth.AuthContext;
import com.softpeces.auth.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblMsg;
    @FXML private CheckBox chkRecordar;

    @FXML
    private void login() {
        String u = txtUser.getText() == null ? "" : txtUser.getText().trim();
        String p = txtPass.getText() == null ? "" : txtPass.getText().trim();
    
        // Limpia mensaje previo
        lblMsg.setText("");
    
        if (u.isBlank() || p.isBlank()) {
            lblMsg.setText("Ingresa usuario y contraseña.");
            return;
        }
    
        try {
            System.out.println("Iniciando autenticación para usuario: " + u);
            // Autenticar
            AuthService auth = new AuthService();
            AuthService.Session session = auth.login(u, p); // lanza excepción si no valida
            System.out.println("Autenticación exitosa para usuario: " + u);
    
            // Guardar sesión para mostrar nombre/correo en el sidebar
            AuthContext.set(session);
    
            try {
                System.out.println("Cargando MainView.fxml...");
                // Abrir la app principal
                var fxml = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
                System.out.println("Cargando el FXML...");
                var scene = new Scene(fxml.load(), 1280, 800);
                System.out.println("FXML cargado correctamente");
                
                Stage stage = (Stage) txtUser.getScene().getWindow();
                System.out.println("Configurando escena...");
                stage.setTitle("Soft Peces");
                stage.setScene(scene);
                System.out.println("Escena configurada, mostrando ventana...");
                stage.show(); // Asegurarse de que la ventana se muestre
                System.out.println("Ventana mostrada");
            } catch (Exception e) {
                System.err.println("Error al cargar la vista principal:");
                e.printStackTrace();
                lblMsg.setText("Error al cargar la aplicación. Ver consola para más detalles.");
            }
    
        } catch (Exception ex) {
            System.err.println("Error en el login:");
            ex.printStackTrace();
            lblMsg.setText("Error: " + ex.getMessage());
        }
    }

}
