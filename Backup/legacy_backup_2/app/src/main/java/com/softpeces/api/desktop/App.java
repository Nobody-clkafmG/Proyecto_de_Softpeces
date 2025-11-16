package com.softpeces.api.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("SoftPeces Desktop");
        stage.setScene(new Scene(new Label("Hola, JavaFX + Gradle!"), 400, 200));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // Necesario para JavaFX
    }
}

