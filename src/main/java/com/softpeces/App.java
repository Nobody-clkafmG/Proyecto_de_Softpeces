package com.softpeces;

import com.softpeces.infra.DatabaseBootstrap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // Configurar manejador de excepciones no capturadas
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Se produjo un error inesperado");
                    alert.setContentText(throwable.getMessage());
                    
                    // Mostrar el stack trace en la consola
                    throwable.printStackTrace();
                    
                    // Mostrar el diálogo de error
                    alert.showAndWait();
                });
            });

            // Cargar la vista de login
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/ui/LoginView.fxml"));
            stage.setTitle("Soft Peces — Iniciar sesión");
            stage.setScene(new Scene(fxml.load(), 560, 420));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error crítico");
            alert.setHeaderText("No se pudo iniciar la aplicación");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        try {
            // Inicializar la base de datos
            System.out.println("Inicializando base de datos...");
            DatabaseBootstrap.init();
            System.out.println("Base de datos inicializada correctamente");
            
            // Iniciar la aplicación JavaFX
            System.out.println("Iniciando aplicación...");
            launch(args);
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación:");
            e.printStackTrace();
        }
    }
}
