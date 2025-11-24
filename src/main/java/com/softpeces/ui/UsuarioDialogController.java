package com.softpeces.ui;

import com.softpeces.auth.UserRepository;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Objects;

public class UsuarioDialogController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtConfirmPasswordVisible;
    @FXML private ToggleButton btnShowNewPassword;
    @FXML private ToggleButton btnShowConfirmPassword;
    @FXML private CheckBox chkAdmin;
    @FXML private CheckBox chkOperador;
    @FXML private CheckBox chkInspector;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblError;
    @FXML private Label lblHint;
    
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private int userId = -1;

    public record FormData(
        int userId,
        String username, 
        String email, 
        String newPassword,
        boolean admin, 
        boolean operador, 
        boolean inspector,
        boolean active
    ) {
        public boolean hasNewPassword() {
            return newPassword != null && !newPassword.isBlank();
        }
    }

    @FXML
    public void initialize() {
        bindPasswordToggle(txtPassword, txtPasswordVisible, btnShowNewPassword);
        bindPasswordToggle(txtConfirmPassword, txtConfirmPasswordVisible, btnShowConfirmPassword);

        // Update hint text based on mode
        lblHint.textProperty().bind(Bindings.when(editing)
            .then("Deja la contraseña en blanco para mantener la actual")
            .otherwise("La contraseña es obligatoria para nuevos usuarios"));

        clearError();
    }

    private void bindPasswordToggle(PasswordField passwordField, TextField visibleField, ToggleButton toggle) {
        Bindings.bindBidirectional(passwordField.textProperty(), visibleField.textProperty());
        visibleField.managedProperty().bind(toggle.selectedProperty());
        visibleField.visibleProperty().bind(toggle.selectedProperty());
        passwordField.managedProperty().bind(toggle.selectedProperty().not());
        passwordField.visibleProperty().bind(toggle.selectedProperty().not());
        toggle.setText("👁");
        toggle.selectedProperty().addListener((obs, oldVal, selected) -> toggle.setText(selected ? "🙈" : "👁"));
    }

    public void configure(UserRepository.UserRow row) {
        if (row != null) {
            this.userId = row.id();
            this.editing.set(true);
            txtUsername.setText(row.username());
            txtEmail.setText(row.email() != null ? row.email() : "");
            chkAdmin.setSelected(row.roles().contains("ADMIN"));
            chkOperador.setSelected(row.roles().contains("OPERADOR"));
            chkInspector.setSelected(row.roles().contains("INSPECTOR"));
            chkActivo.setSelected(row.active());
        } else {
            this.editing.set(false);
            chkActivo.setSelected(true);
        }
        clearPasswords();
        btnShowNewPassword.setSelected(false);
        btnShowConfirmPassword.setSelected(false);
    }

    public boolean validateForm() {
        clearError();

        // Validate username
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        if (username.isEmpty()) {
            showError("El nombre de usuario es obligatorio.");
            return false;
        }
        
        // For new users or when changing password
        if (!editing.get() || hasNewPassword()) {
            // For new users, new password is required
            if (!editing.get() && getNewPassword().isEmpty()) {
                showError("La contraseña es obligatoria para nuevos usuarios.");
                return false;
            }
            
            // If new password is provided, it must match confirmation
            if (!getNewPassword().isEmpty() && !Objects.equals(getNewPassword(), getConfirmPassword())) {
                showError("Las contraseñas no coinciden.");
                return false;
            }
            
        }
        
        return true;
    }

    public FormData getFormData() {
        return new FormData(
            userId,
            txtUsername.getText().trim(),
            txtEmail.getText().trim(),
            getNewPassword(),
            chkAdmin.isSelected(),
            chkOperador.isSelected(),
            chkInspector.isSelected(),
            chkActivo.isSelected()
        );
    }
    
    public boolean hasNewPassword() {
        return !getNewPassword().isEmpty();
    }

    public void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    
    private String getNewPassword() {
        String pass = txtPassword.getText();
        return pass != null ? pass.trim() : "";
    }

    private String getConfirmPassword() {
        String pass = txtConfirmPassword.getText();
        return pass != null ? pass.trim() : "";
    }

    private void clearPasswords() {
        
        txtPassword.clear();
        txtPasswordVisible.clear();
        txtConfirmPassword.clear();
        txtConfirmPasswordVisible.clear();
    }

    private void clearError() {
        lblError.setText("");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
