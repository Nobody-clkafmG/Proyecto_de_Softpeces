package com.softpeces.ui;

import com.softpeces.model.SoftwarePiscicultura;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaController {

    @FXML private TableView<SoftwarePiscicultura> tblSoftware;
    @FXML private TableColumn<SoftwarePiscicultura, String> colNombre;
    @FXML private TableColumn<SoftwarePiscicultura, String> colLicencia;
    @FXML private TableColumn<SoftwarePiscicultura, String> colIA;
    @FXML private TableColumn<SoftwarePiscicultura, String> colEnfoque;
    @FXML private TableColumn<SoftwarePiscicultura, String> colDescripcion;
    @FXML private TableColumn<SoftwarePiscicultura, String> colEnlace;
    @FXML private Label lblMensaje;

    private final ObservableList<SoftwarePiscicultura> softwareList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colLicencia.setCellValueFactory(new PropertyValueFactory<>("licencia"));
        colIA.setCellValueFactory(new PropertyValueFactory<>("usaIA"));
        colEnfoque.setCellValueFactory(new PropertyValueFactory<>("enfoque"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEnlace.setCellValueFactory(new PropertyValueFactory<>("enlace"));

        // Permitir que el texto se envuelva en varias líneas
        enableWrapping(colNombre);
        enableWrapping(colLicencia);
        enableWrapping(colIA);
        enableWrapping(colEnfoque);
        enableWrapping(colDescripcion);

        setupLinkColumn();

        // Configurar la tabla para que las columnas se ajusten al contenido
        tblSoftware.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tblSoftware.setFixedCellSize(-1);
        
        // Cargar los datos del archivo CSV
        cargarDatosCSV();
    }

    private void cargarDatosCSV() {
        List<SoftwarePiscicultura> listaSoftware = new ArrayList<>();
        
        // Primero, intentar cargar desde el classpath (para JAR)
        try (InputStream is = getClass().getResourceAsStream("/data/biblioteca_software_piscicultura.csv")) {
            if (is == null) {
                throw new FileNotFoundException("No se pudo encontrar el archivo de recursos");
            }
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                // Saltar la primera línea (encabezados)
                String line = br.readLine();
                
                while ((line = br.readLine()) != null) {
                    // Dividir por punto y coma ignorando los que estén dentro de comillas
                    String[] values = line.split(";(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    
                    if (values.length >= 7) {
                        // Limpiar comillas dobles si existen
                        String nombre = values[1].replaceAll("^\"|\"$", "");
                        String licencia = values[2].replaceAll("^\"|\"$", "");
                        String usaIA = values[3].replaceAll("^\"|\"$", "");
                        String enfoque = values[4].replaceAll("^\"|\"$", "");
                        String descripcion = values[5].replaceAll("^\"|\"$", "");
                        String enlace = values[6].replaceAll("^\"|\"$", "");
                        
                        listaSoftware.add(new SoftwarePiscicultura(nombre, licencia, usaIA, enfoque, descripcion, enlace));
                    }
                }
                
                // Actualizar la tabla
                actualizarTabla(listaSoftware);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar desde recursos: " + e.getMessage());
            e.printStackTrace();
            cargarDatosEjemplo();
        }
    }
    
    private void actualizarTabla(List<SoftwarePiscicultura> datos) {
        Platform.runLater(() -> {
            softwareList.setAll(datos);
            tblSoftware.setItems(softwareList);
        });
    }

    private void cargarDatosEjemplo() {
        // Datos de ejemplo en caso de que falle la carga del CSV
        List<SoftwarePiscicultura> ejemplos = new ArrayList<>();
        ejemplos.add(new SoftwarePiscicultura(
            "AquaTracker", 
            "Propietario, de bajo costo (SaaS)", 
            "No explícito", 
            "Gestión de producción acuícola en la nube", 
            "Permite llevar registros básicos de lotes de peces, alimentación, biomasa y costos.",
            "https://www.aquatracker.com"
        ));
        
        ejemplos.add(new SoftwarePiscicultura(
            "navfarm (Aquaculture)", 
            "Propietario, suscripción", 
            "No explícito", 
            "Gestión de estanques y lotes de peces en tiempo real", 
            "Desde el celular se pueden registrar siembras, mortalidades, alimentación y tratamientos por estanque.",
            "https://www.navfarm.com/aquaculture"
        ));
        
        actualizarTabla(ejemplos);
        mostrarMensaje("Se están mostrando datos de ejemplo", false);
    }

    @FXML
    private void exportarACSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar archivo de software");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"));
        fileChooser.setInitialFileName("biblioteca_software_piscicultura.csv");
        
        File file = fileChooser.showSaveDialog(tblSoftware.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                // Escribir encabezados
                writer.println("Software / Servicio;Licencia / costo aproximado;¿Usa IA?;Enfoque principal;¿Cómo ayuda al pequeño piscicultor?;Enlace");
                
                // Escribir datos
                for (SoftwarePiscicultura software : softwareList) {
                    writer.println(String.format("%s;%s;%s;%s;%s;%s",
                        escapeCsv(software.getNombre()),
                        escapeCsv(software.getLicencia()),
                        escapeCsv(software.getUsaIA()),
                        escapeCsv(software.getEnfoque()),
                        escapeCsv(software.getDescripcion()),
                        escapeCsv(software.getEnlace())
                    ));
                }
                
                mostrarMensaje("Datos exportados correctamente a " + file.getAbsolutePath(), false);
            } catch (IOException e) {
                e.printStackTrace();
                mostrarMensaje("Error al exportar los datos: " + e.getMessage(), true);
            }
        }
    }
    
    private String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        // Si el texto contiene punto y coma, saltos de línea o comillas dobles, rodearlo con comillas dobles
        if (input.contains(";") || input.contains("\n") || input.contains("\"")) {
            // Escapar comillas dobles duplicándolas
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
    
    private void mostrarMensaje(String mensaje, boolean esError) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setStyle(esError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        }
    }

    private void enableWrapping(TableColumn<SoftwarePiscicultura, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            private final Text text = new Text();
            {
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));
                setGraphic(text);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText(null);
                } else {
                    text.setText(item);
                }
            }
        });
    }

    private void setupLinkColumn() {
        colEnlace.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();

            {
                link.setWrapText(true);
                setGraphic(link);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                link.setOnAction(evt -> abrirEnlace(link.getText()));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                } else {
                    link.setText(item);
                    setGraphic(link);
                }
            }
        });
    }

    private void abrirEnlace(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            URI uri = new URI(url);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
            } else {
                mostrarMensaje("No se puede abrir el enlace en este sistema.", true);
            }
        } catch (Exception e) {
            mostrarMensaje("Error al abrir el enlace: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
}
