package semana12;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generador de datos de prueba para reservas
 */
public class TestReservaDataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TestReservaDataGenerator.class);

    private final Random random = new Random();

    /**
     * Genera una lista de reservas de prueba
     */
    public List<Reserva> generateTestReservas(int count) {
        System.out.println("🔄 Generando " + count + " reservas de prueba...");

        List<Reserva> reservas = new ArrayList<>();

        int generated = 0;
        while (generated < count) {
            Reserva reserva = generateRandomReserva();

            // Validar que la reserva esté completa antes de agregarla
            if (isReservaComplete(reserva)) {
                reservas.add(reserva);
                generated++;

                if (generated % 10 == 0) {
                    System.out.println("📊 Generadas: " + generated + " reservas");
                }
            } else {
                logger.warn("Reserva incompleta generada, reintentando...");
            }
        }

        System.out.println("✅ Generación completada: " + reservas.size() + " reservas");
        return reservas;
    }

    /**
     * Valida que una reserva tenga todos los datos necesarios
     */
    private boolean isReservaComplete(Reserva reserva) {
        return reserva != null &&
               reserva.getIdReserva() != null &&
               reserva.getUsuarioId() != null && reserva.getUsuarioId() > 0 &&
               reserva.getLocalId() != null && reserva.getLocalId() > 0 &&
               reserva.getFechaInicio() != null &&
               reserva.getFechaFin() != null &&
               reserva.getMonto() != null && reserva.getMonto() > 0 &&
               reserva.getEstado() != null;
    }

    /**
     * Genera una reserva aleatoria con datos realistas
     */
    private Reserva generateRandomReserva() {
        Reserva reserva = new Reserva();

        try {
            // ID de usuario y local aleatorios entre 1 y 50 / 1 y 20
            reserva.setUsuarioId(1 + random.nextInt(50));
            reserva.setLocalId(1 + random.nextInt(20));

            // Fecha de inicio aleatoria dentro de los próximos 30 días, a las 18:00
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = now.plusDays(random.nextInt(30)).withHour(18).withMinute(0).withSecond(0).withNano(0);
            reserva.setFechaInicio(startDate);

            // Fecha de fin 4 a 10 horas después
            LocalDateTime endDate = startDate.plusHours(4 + random.nextInt(7));
            reserva.setFechaFin(endDate);

            // Monto aleatorio entre 500 y 5000, redondeado a múltiplos de 10
            double monto = 500 + (random.nextDouble() * 4500);
            monto = Math.round(monto / 10.0) * 10;
            reserva.setMonto(monto);

            // Estado aleatorio
            EstadoReserva[] estados = EstadoReserva.values();
            reserva.setEstado(estados[random.nextInt(estados.length)]);

            return reserva;

        } catch (Exception e) {
            logger.error("❌ Error generando reserva de prueba: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Método main para prueba de generación en consola
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║     GENERADOR DE RESERVAS DE PRUEBA║");
        System.out.println("╚════════════════════════════════════╝");

        TestReservaDataGenerator generator = new TestReservaDataGenerator();

        System.out.println("\n--- Generando reservas de prueba ---");
        List<Reserva> reservas = generator.generateTestReservas(20);

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║        GENERACIÓN COMPLETADA       ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("📊 Total de reservas generadas: " + reservas.size());
    }
}


