package com.softpeces;

import com.softpeces.infra.DatabaseBootstrap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/ui/LoginView.fxml"));
        stage.setTitle("Soft Peces — Iniciar sesión");
        stage.setScene(new Scene(fxml.load(), 420, 260));
        stage.show();
    }

    public static void main(String[] args) {
        DatabaseBootstrap.init();   // crea DB y seeds
        launch(args);
    }
}
