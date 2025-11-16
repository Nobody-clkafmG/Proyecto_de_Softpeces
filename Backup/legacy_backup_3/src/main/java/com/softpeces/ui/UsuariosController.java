package com.softpeces.ui;

import com.softpeces.auth.UserRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsuariosController {

    @FXML private TableView<UserRepository.UserRow> tblUsuarios;
    @FXML private TableColumn<UserRepository.UserRow, Integer> colId;
    @FXML private TableColumn<UserRepository.UserRow, String> colUsername;
    @FXML private TableColumn<UserRepository.UserRow, String> colActivo;
    @FXML private TableColumn<UserRepository.UserRow, String> colRoles;
    @FXML private Label lblMsg;
    @FXML private CheckBox chkAdmin;
    @FXML private CheckBox chkOperador;

    private final UserRepository users = new UserRepository();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().username()));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().active() ? "Sí" : "No"));
        colRoles.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.join(", ", c.getValue().roles())));
        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> syncRoleCheckboxes(row));
        recargarTabla();
    }

    private void recargarTabla() {
        tblUsuarios.setItems(FXCollections.observableArrayList(users.findAll()));
        lblMsg.setText("");
    }

    private void syncRoleCheckboxes(UserRepository.UserRow row) {
        if (row == null) {
            chkAdmin.setSelected(false);
            chkOperador.setSelected(false);
            return;
        }
        chkAdmin.setSelected(row.roles().contains("ADMIN"));
        chkOperador.setSelected(row.roles().contains("OPERADOR"));
    }

    @FXML
    public void onNuevoUsuario() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Nuevo usuario");
        d.setHeaderText("Crear usuario");
        d.setContentText("Nombre de usuario:");
        var userOpt = d.showAndWait();
        if (userOpt.isEmpty() || userOpt.get().isBlank()) return;

        TextInputDialog passDlg = new TextInputDialog();
        passDlg.setTitle("Nuevo usuario");
        passDlg.setHeaderText("Definir contraseña temporal");
        passDlg.setContentText("Contraseña:");
        var passOpt = passDlg.showAndWait();
        if (passOpt.isEmpty()) return;

        try {
            users.createUser(userOpt.get().trim(), passOpt.get(), chkAdmin.isSelected(), chkOperador.isSelected());
            lblMsg.setText("Usuario creado correctamente.");
            recargarTabla();
        } catch (Exception e) {
            lblMsg.setText("Error creando usuario: " + e.getMessage());
        }
    }

    @FXML
    public void onResetPassword() {
        var row = tblUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) { lblMsg.setText("Selecciona un usuario."); return; }
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Resetear contraseña");
        d.setHeaderText("Nueva contraseña para " + row.username());
        d.setContentText("Nueva contraseña:");
        var resp = d.showAndWait();
        if (resp.isEmpty()) return;
        try {
            users.resetPassword(row.id(), resp.get());
            lblMsg.setText("Contraseña actualizada.");
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onActivar() { setActivo(true); }

    @FXML
    public void onDesactivar() { setActivo(false); }

    private void setActivo(boolean activo) {
        var row = tblUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) { lblMsg.setText("Selecciona un usuario."); return; }
        try {
            users.setActive(row.id(), activo);
            lblMsg.setText(activo ? "Usuario activado." : "Usuario desactivado.");
            recargarTabla();
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onToggleAdmin() { updateRolesFromCheckboxes(); }

    @FXML
    public void onToggleOperador() { updateRolesFromCheckboxes(); }

    private void updateRolesFromCheckboxes() {
        var row = tblUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) return;
        try {
            users.updateRoles(row.id(), chkAdmin.isSelected(), chkOperador.isSelected());
            lblMsg.setText("Roles actualizados.");
            recargarTabla();
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
        }
    }
}
