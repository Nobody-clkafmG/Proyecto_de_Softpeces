package com.softpeces.ui;

import com.softpeces.auth.AuthContext;
import com.softpeces.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;


public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblMsg;

    private final AuthService auth = new AuthService();

    @FXML
    public void initialize() {
        if (lblMsg != null) lblMsg.setText("");
    }

    @FXML
    public void onIngresar() {
        lblMsg.setText("");
        try {
            var session = auth.login(txtUser.getText().trim(), txtPass.getText());
            AuthContext.set(session);

            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
            Parent root = fxml.load();                 // ← aquí el cambio
            MainController controller = fxml.getController();
            controller.initSession(session);

            Stage stage = (Stage) txtUser.getScene().getWindow();
            stage.setTitle("Soft Peces — Principal");
            stage.setScene(new Scene(root, 1024, 700));
            stage.show();

        } catch (Exception e) {
            lblMsg.setText(e.getMessage());
        }
    }
}
