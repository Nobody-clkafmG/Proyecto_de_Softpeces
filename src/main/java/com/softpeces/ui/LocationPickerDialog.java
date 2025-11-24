package com.softpeces.ui;

import javafx.concurrent.Worker;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.Optional;

import com.softpeces.ui.LocationPickerDialog.JavaConnector;

public class LocationPickerDialog extends Dialog<LocationPickerDialog.GeoLocation> {

    public record GeoLocation(double lat, double lon) {}

    private final WebView webView = new WebView();
    private GeoLocation selected;

    public LocationPickerDialog(Double initialLat, Double initialLon) {
    setTitle("Seleccionar ubicación");
    setHeaderText("Haz clic en el mapa para marcar la ubicación de la estación.");

    webView.setPrefWidth(800);
    webView.setPrefHeight(600);

    WebEngine engine = webView.getEngine();

    URL url = getClass().getResource("/map/location_picker.html");
    if (url == null) {
        throw new IllegalStateException("No se encontró /map/location_picker.html en resources");
    }
    engine.load(url.toExternalForm());

    // 👇 NUEVO: forzar a Leaflet a recalcular cuando cambie el tamaño del WebView
    webView.widthProperty().addListener((obs, oldV, newV) -> {
        try {
            engine.executeScript("if (window.map) { window.map.invalidateSize(); }");
        } catch (Exception ignored) {}
    });
    webView.heightProperty().addListener((obs, oldV, newV) -> {
        try {
            engine.executeScript("if (window.map) { window.map.invalidateSize(); }");
        } catch (Exception ignored) {}
    });

    engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
        if (newState == Worker.State.SUCCEEDED) {
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("javaConnector", new JavaConnector());

            if (initialLat != null && initialLon != null) {
                String script = String.format("setInitialLocation(%f, %f);",
                        initialLat, initialLon);
                engine.executeScript(script);
            }
        }
    });

    getDialogPane().setContent(webView);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    setResultConverter(button -> {
        if (button == ButtonType.OK && selected != null) {
            return selected;
        }
        return null;
    });
}


    public Optional<GeoLocation> showDialog() {
        return this.showAndWait();
    }

    public class JavaConnector {
        // Lo llama el JS cuando haces clic en el mapa
        public void locationSelected(String latStr, String lonStr) {
            try {
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                selected = new GeoLocation(lat, lon);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
