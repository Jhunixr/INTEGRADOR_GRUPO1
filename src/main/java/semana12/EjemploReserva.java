package semana12;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Clase principal para probar la integración de tus clases de Reserva.
 * Demuestra:
 *  - SLF4J (logging)
 *  - Guava (Listas)
 *  - Apache Commons Lang3 (StringUtils)
 *  - Validación de Reserva
 *  - StatisticsService
 *  - Escritura a Excel
 */
public class EjemploReserva {

    private static final Logger logger = LoggerFactory.getLogger(EjemploReserva.class);

    public static void main(String[] args) {
        logger.info("=== INICIANDO PRUEBAS DE RESERVA ===");

        // 1) Generar datos de prueba
        logger.info("--- Generando reservas de prueba con TestReservaDataGenerator ---");
        TestReservaDataGenerator generator = new TestReservaDataGenerator();
        List<Reserva> reservas = generator.generateTestReservas(10);
        logger.info("Se generaron {} reservas de prueba", reservas.size());

        // 2) Mostrar reservas originales
        logger.info("\n--- Reservas originales (orden de generación) ---");
        reservas.forEach(r ->
            logger.info("  • ID={} | Usuario={} | Local={} | Inicio={} | Monto={}",
                r.getIdReserva(),
                r.getUsuarioId(),
                r.getLocalId(),
                r.getFechaInicio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                String.format("%,.2f", r.getMonto()))
        );

        // 3) Validar reservas
        logger.info("\n--- Validando reservas ---");
        ReservaValidator validator = new ReservaValidator();
        List<Reserva> validas = Lists.newArrayList();
        for (Reserva r : reservas) {
            var errs = validator.validate(r);
            if (errs.isEmpty()) {
                validas.add(r);
            } else {
                logger.warn("Reserva {} inválida: {}", r.getIdReserva(), StringUtils.join(errs, "; "));
            }
        }
        logger.info("Reservas válidas: {}", validas.size());

        // 4) Orden natural (fechaInicio ascendente)
        logger.info("\n--- Orden natural (fechaInicio asc) ---");
        Collections.sort(validas);
        validas.forEach(r ->
            logger.info("  • ID={} | Inicio={}", r.getIdReserva(),
                r.getFechaInicio().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        );

        // 5) Orden personalizado (monto descendente)
        logger.info("\n--- Orden personalizado (monto desc) ---");
        Comparator<Reserva> montoDesc = Comparator.comparing(Reserva::getMonto, Comparator.nullsLast(Double::compareTo)).reversed();
        Collections.sort(validas, montoDesc);
        validas.forEach(r ->
            logger.info("  • ID={} | Monto={}", r.getIdReserva(), String.format("%,.2f", r.getMonto()))
        );

        // 6) Estadísticas
        logger.info("\n--- Generando estadísticas ---");
        ReservaStatisticsService statsSvc = new ReservaStatisticsService();
        var summary = statsSvc.createSummary(validas);
        logger.info("TOTAL reservas: {}", summary.getTotalReservas());
        logger.info("PROMEDIO monto: {}", String.format("%,.2f", summary.getAverageMonto()));
        logger.info("PROMEDIO duración (hrs): {}", String.format("%.1f", summary.getAverageDuracionHoras()));
        logger.info("ESTADOS encontrados: {}", StringUtils.join(summary.getEstados(), ", "));

        // 7) Filtrar (ejemplo: monto >= 1000)
        logger.info("\n--- Filtrando reservas con monto >= 1000 ---");
        List<Reserva> grandes = statsSvc.filterReservas(validas, 1000.0, null, null);
        grandes.forEach(r ->
            logger.info("  • ID={} | Monto={}", r.getIdReserva(), String.format("%,.2f", r.getMonto()))
        );

        // 8) Guardar a Excel
        logger.info("\n--- Guardando reservas válidas a Excel ---");
        ExcelService excel = new ExcelService();
        String outPath = "data/reservas_ejemplo_" +
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                         ".xlsx";
        try {
            File dataDir = new File("data");
            if (!dataDir.exists() && dataDir.mkdirs()) {
                logger.info("Directorio 'data' creado: {}", dataDir.getAbsolutePath());
            }
            excel.writeReservasToExcel(validas, outPath);
            logger.info("✅ Reservas escritas en: {}", outPath);
        } catch (IOException e) {
            logger.error("❌ Error al escribir Excel: {}", e.getMessage(), e);
        }

        logger.info("=== PRUEBAS DE RESERVA COMPLETADAS ===");
    }
}

