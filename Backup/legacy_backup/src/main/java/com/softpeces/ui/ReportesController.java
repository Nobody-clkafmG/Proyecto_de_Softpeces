package com.softpeces.ui;

import com.softpeces.catalog.Estacion;
import com.softpeces.catalog.EstacionRepository;
import com.softpeces.domain.Lote;
import com.softpeces.reports.ReportRepository;
import com.softpeces.reports.ReportRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.FileWriter;
import java.util.List;

public class ReportesController {

    @FXML private DatePicker dpDesde, dpHasta;
    @FXML private ComboBox<Lote> cbLote;
    @FXML private ComboBox<Estacion> cbEstacion;
    @FXML private TableView<ReportRow> tbl;
    @FXML private TableColumn<ReportRow,Integer> colMID;
    @FXML private TableColumn<ReportRow,String> colFecha, colLote, colEst, colTanq, colParte, colQC, colEstado, colLabel, colRuta;
    @FXML private TableColumn<ReportRow,Double> colProb;
    @FXML private Label lblMsg;

    private final com.softpeces.repo.LoteRepository lotes = new com.softpeces.repo.LoteRepository();
    private final EstacionRepository estRepo = new EstacionRepository();
    private final ReportRepository repo = new ReportRepository();

    @FXML
    public void initialize() {
        cbLote.setItems(FXCollections.observableArrayList(lotes.findAll()));
        cbLote.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Lote l){ return l==null? "" : l.id()+" - "+l.nombre(); }
            public Lote fromString(String s){ return null; }
        });
        cbEstacion.setItems(FXCollections.observableArrayList(estRepo.findAll()));
        cbEstacion.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Estacion e){ return e==null? "" : e.nombre(); }
            public Estacion fromString(String s){ return null; }
        });

        colMID.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().muestreoId()));
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().fechaHora()));
        colLote.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().loteNombre()));
        colEst.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().estacionNombre()));
        colTanq.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().tanqueCodigo()));
        colParte.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().parte()));
        colQC.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().qc()));
        colEstado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().estado()));
        colLabel.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().label()));
        colProb.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().prob()));
        colRuta.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().ruta()));
    }

    @FXML public void onBuscar() {
        Integer lid = cbLote.getValue()==null? null : cbLote.getValue().id();
        Integer eid = cbEstacion.getValue()==null? null : cbEstacion.getValue().id();
        var data = repo.search(lid, eid, dpDesde.getValue(), dpHasta.getValue());
        tbl.setItems(FXCollections.observableArrayList(data));
        lblMsg.setText("Filas: " + data.size());
    }

    @FXML public void onExportar() {
        List<ReportRow> data = tbl.getItems();
        if (data==null || data.isEmpty()) { lblMsg.setText("No hay datos para exportar."); return; }

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("reporte_softpeces.csv");
        var f = fc.showSaveDialog(tbl.getScene().getWindow());
        if (f == null) return;

        try (FileWriter w = new FileWriter(f, java.nio.charset.StandardCharsets.UTF_8)) {
            w.write("Muestreo,FechaHora,Lote,Estacion,Tanque,Parte,QC,Estado,Label,Prob,Ruta\n");
            for (var r: data) {
                w.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        r.muestreoId(), esc(r.fechaHora()), esc(r.loteNombre()), esc(r.estacionNombre()),
                        esc(r.tanqueCodigo()), r.parte(), r.qc(), r.estado(),
                        esc(r.label()), r.prob()==null? "": r.prob().toString(), esc(r.ruta())));
            }
            lblMsg.setText("CSV exportado: " + f.getAbsolutePath());
        } catch (Exception e) {
            lblMsg.setText("Error exportando: " + e.getMessage());
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        s = s.replace("\"","\"\"");
        // rodeamos de comillas por si hay comas
        return "\""+s+"\"";
    }
}
