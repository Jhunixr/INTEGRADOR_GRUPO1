package com.utpintegrador.semana10;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    private final Random random = new Random();

    private final String[] ubicaciones = {"Chimbote", "Nuevo Chimbote", "Santa", "Coishco", "Moro"};
    private final String[] temas = {"Concierto", "Conferencia", "Taller", "Seminario", "Feria"};

    public List<Evento> generarEventosDemo(int cantidad) {
        List<Evento> eventos = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            eventos.add(new Evento(
                (long) i,
                temas[random.nextInt(temas.length)] + " #" + i,
                "Descripción del evento " + i,
                50 + random.nextInt(451), // 50-500
                (double) (100 + random.nextInt(901)), // 100-1000
                ubicaciones[random.nextInt(ubicaciones.length)],
                LocalDate.now().plusDays(random.nextInt(365)),
                Evento.Estado.values()[random.nextInt(Evento.Estado.values().length)]
            ));
        }
        return eventos;
    }

    public void generarArchivoDemo(String filePath, int cantidad) throws IOException {
        logger.info("Generando archivo demo con {} eventos: {}", cantidad, filePath);
        List<Evento> eventos = generarEventosDemo(cantidad);
        EventoExcelService excelService = new EventoExcelService();
        excelService.escribirEventosAExcel(eventos, filePath);
    }
}