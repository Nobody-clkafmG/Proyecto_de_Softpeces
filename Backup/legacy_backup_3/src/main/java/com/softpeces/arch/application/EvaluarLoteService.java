package com.softpeces.arch.application;

import com.softpeces.reports.ReportRepository;
import com.softpeces.reports.ReportRow;
import java.util.List;

/** Simple domain service that evaluates a lote using current foto labels/qc. */
public class EvaluarLoteService {
    private final ReportRepository repo = new ReportRepository();

    public String evaluarAptitud(int loteId) {
        List<ReportRow> rows = repo.search(loteId, null, null, null);
        if (rows.isEmpty()) return "SIN_DATOS";
        long ok = rows.stream().filter(r -> "OK".equalsIgnoreCase(r.qc())).count();
        double ratio = ok * 1.0 / rows.size();
        return ratio >= 0.7 ? "APTA" : "NO_APTA";
    }
}