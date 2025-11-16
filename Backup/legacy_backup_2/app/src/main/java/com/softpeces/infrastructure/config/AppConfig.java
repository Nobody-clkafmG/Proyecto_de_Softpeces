package com.softpeces.infrastructure.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.softpeces.domain.repository.FotoStorage;
import com.softpeces.domain.repository.LoteRepository;
import com.softpeces.domain.service.ReglaClasificacion;
import com.softpeces.infrastructure.ai.IAReglaClasificacionONNX;
import com.softpeces.infrastructure.ai.ModeloProvider;
import com.softpeces.infrastructure.ai.PostprocesadorSalida;
import com.softpeces.infrastructure.ai.PreprocesadorImagen;
import com.softpeces.infrastructure.io.LocalFileFotoStorage;
import com.softpeces.infrastructure.persistence.jdbc.JdbcLoteRepository;

@Configuration
public class AppConfig {

    // Bean de repositorio (stub)
    @Bean
    public LoteRepository loteRepository() {
        return new JdbcLoteRepository(null); // luego cambia a JdbcTemplate real
    }

    // Bean de storage de fotos
    @Bean
    public FotoStorage fotoStorage() {
        return new LocalFileFotoStorage(Path.of("data/fotos"));
    }

    // Bean de IA (stub ONNX)
    @Bean
    public ReglaClasificacion reglaClasificacion() {
        return new IAReglaClasificacionONNX(
                new ModeloProvider("data/models/model.onnx", "v1"),
                new PreprocesadorImagen(),
                new PostprocesadorSalida()
        );
    }
}
