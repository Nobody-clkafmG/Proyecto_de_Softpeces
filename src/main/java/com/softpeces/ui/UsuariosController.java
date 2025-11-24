package com.softpeces.ui;

import com.softpeces.auth.UserRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional;

public class UsuariosController {

    @FXML private TableView<UserRepository.UserRow> tblUsuarios;
    @FXML private TableColumn<UserRepository.UserRow, Integer> colId;
    @FXML private TableColumn<UserRepository.UserRow, String> colUsername;
    @FXML private TableColumn<UserRepository.UserRow, String> colEmail;
    @FXML private TableColumn<UserRepository.UserRow, String> colActivo;
    @FXML private TableColumn<UserRepository.UserRow, String> colRoles;
    @FXML private Label lblMsg;

    private final UserRepository users = new UserRepository();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().id()));
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().username()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().email() == null ? "" : c.getValue().email()
        ));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().active() ? "Sí" : "No"));
        colRoles.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.join(", ", c.getValue().roles())));
        recargarTabla();
    }

    private void recargarTabla() {
        tblUsuarios.setItems(FXCollections.observableArrayList(users.findAll()));
        lblMsg.setText("");
    }

    @FXML
    public void onNuevoUsuario() {
        mostrarDialogoUsuario(null).ifPresent(data -> {
            try {
                if (!data.hasNewPassword()) {
                    lblMsg.setText("Debe ingresar una contraseña para crear el usuario.");
                    return;
                }
                users.createUser(data.username(), data.email(), data.newPassword(),
                        data.admin(), data.operador(), data.inspector(), data.active());
                lblMsg.setText("Usuario creado correctamente.");
                recargarTabla();
            } catch (Exception e) {
                lblMsg.setText("Error creando usuario: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onEditarUsuario() {
        var row = tblUsuarios.getSelectionModel().getSelectedItem();
        if (row == null) {
            lblMsg.setText("Selecciona un usuario.");
            return;
        }

        mostrarDialogoUsuario(row).ifPresent(data -> {
            try {
                if (data.hasNewPassword()) {
                    users.resetPassword(row.id(), data.newPassword());
                }
                users.updateUser(row.id(), data.username(), data.email(), data.active());
                users.updateRoles(row.id(), data.admin(), data.operador(), data.inspector());
                lblMsg.setText("Usuario actualizado.");
                recargarTabla();
            } catch (Exception e) {
                lblMsg.setText("Error actualizando usuario: " + e.getMessage());
                e.printStackTrace();
            }
    });
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

    private Optional<UsuarioDialogController.FormData> mostrarDialogoUsuario(UserRepository.UserRow row) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/UsuarioDialog.fxml"));
            DialogPane pane = loader.load();
            UsuarioDialogController controller = loader.getController();
            controller.configure(row);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(row == null ? "Nuevo usuario" : "Editar usuario");
            dialog.setDialogPane(pane);
            dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Guardar");
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (!controller.validateForm()) {
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return Optional.of(controller.getFormData());
            }
        } catch (IOException e) {
            lblMsg.setText("No se pudo abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
