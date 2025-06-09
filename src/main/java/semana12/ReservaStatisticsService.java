package semana12;


import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ReservaStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaStatisticsService.class);

    // Agrupar reservas por estado
    public Multimap<EstadoReserva, Reserva> groupByEstado(List<Reserva> reservas) {
        logger.info("Agrupando {} reservas por estado", reservas.size());

        ImmutableListMultimap.Builder<EstadoReserva, Reserva> builder = ImmutableListMultimap.builder();

        reservas.forEach(res -> {
            if (res.getEstado() != null) {
                builder.put(res.getEstado(), res);
            }
        });

        Multimap<EstadoReserva, Reserva> grouped = builder.build();

        logger.debug("Reservas agrupadas en {} estados", grouped.keySet().size());
        grouped.keySet().forEach(estado -> 
            logger.debug("Estado '{}': {} reservas", estado, grouped.get(estado).size())
        );

        return grouped;
    }

    /**
     * Calcula estadísticas por estado de reserva
     */
    public Map<EstadoReserva, EstadoReservaStats> calculateEstadoStatistics(List<Reserva> reservas) {
        logger.info("Calculando estadísticas por estado de reserva");

        Multimap<EstadoReserva, Reserva> grouped = groupByEstado(reservas);
        Map<EstadoReserva, EstadoReservaStats> statsMap = new HashMap<>();

        for (EstadoReserva estado : grouped.keySet()) {
            Collection<Reserva> reservasEstado = grouped.get(estado);
            EstadoReservaStats stats = calculateStatsForEstado(estado, reservasEstado);
            statsMap.put(estado, stats);
        }

        logger.info("Estadísticas calculadas para {} estados", statsMap.size());
        return statsMap;
    }

    private EstadoReservaStats calculateStatsForEstado(EstadoReserva estado, Collection<Reserva> reservas) {
        List<Reserva> lista = new ArrayList<>(reservas);

        int count = lista.size();

        OptionalDouble avgMonto = lista.stream()
            .filter(r -> r.getMonto() != null)
            .mapToDouble(Reserva::getMonto)
            .average();

        OptionalDouble avgDuracion = lista.stream()
            .mapToLong(Reserva::getDuracionHoras)
            .average();

        Optional<Reserva> maxMontoReserva = lista.stream()
            .filter(r -> r.getMonto() != null)
            .max(Comparator.comparing(Reserva::getMonto));

        Optional<Reserva> maxDuracionReserva = lista.stream()
            .max(Comparator.comparing(Reserva::getDuracionHoras));

        Map<String, Long> montoRanges = calculateMontoRanges(lista);

        logger.debug("Estadísticas para estado '{}': {} reservas", estado, count);

        return new EstadoReservaStats(
            estado,
            count,
            avgMonto.orElse(0.0),
            avgDuracion.orElse(0.0),
            maxMontoReserva.orElse(null),
            maxDuracionReserva.orElse(null),
            montoRanges
        );
    }

    private Map<String, Long> calculateMontoRanges(List<Reserva> reservas) {
        return reservas.stream()
            .filter(r -> r.getMonto() != null)
            .collect(Collectors.groupingBy(
                this::getMontoRange,
                Collectors.counting()
            ));
    }

    private String getMontoRange(Reserva reserva) {
        double monto = reserva.getMonto();
        if (monto < 100) return "Menos de $100";
        else if (monto < 500) return "$100 - $499";
        else if (monto < 1000) return "$500 - $999";
        else return "$1000 o más";
    }

    /**
     * Filtra reservas según monto mínimo, duración mínima (horas) y estado
     */
    public List<Reserva> filterReservas(List<Reserva> reservas,
                                       Double minMonto,
                                       Long minDuracionHoras,
                                       EstadoReserva estado) {
        logger.info("Filtrando reservas con criterios: monto >= {}, duracion >= {}, estado = {}",
            minMonto, minDuracionHoras, estado);

        return reservas.stream()
            .filter(r -> (minMonto == null || (r.getMonto() != null && r.getMonto() >= minMonto)) )
            .filter(r -> (minDuracionHoras == null || r.getDuracionHoras() >= minDuracionHoras) )
            .filter(r -> (estado == null || estado.equals(r.getEstado())) )
            .collect(Collectors.toList());
    }

    /**
     * Obtiene las top N reservas por monto
     */
    public List<Reserva> getTopReservasByMonto(List<Reserva> reservas, int topN) {
        logger.info("Obteniendo top {} reservas por monto", topN);

        return reservas.stream()
            .filter(r -> r.getMonto() != null)
            .sorted(Comparator.comparing(Reserva::getMonto).reversed())
            .limit(topN)
            .collect(Collectors.toList());
    }

    /**
     * Resumen general de reservas
     */
    public ReservaSummary createSummary(List<Reserva> reservas) {
        logger.info("Creando resumen general de {} reservas", reservas.size());

        if (reservas.isEmpty()) {
            return new ReservaSummary(0, 0.0, 0.0, ImmutableSet.of(), ImmutableMap.of());
        }

        int total = reservas.size();

        double avgMonto = reservas.stream()
            .filter(r -> r.getMonto() != null)
            .mapToDouble(Reserva::getMonto)
            .average()
            .orElse(0.0);

        double avgDuracion = reservas.stream()
            .mapToLong(Reserva::getDuracionHoras)
            .average()
            .orElse(0.0);

        ImmutableSet<EstadoReserva> estados = ImmutableSet.copyOf(
            reservas.stream()
                .map(Reserva::getEstado)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
        );

        ImmutableMap<EstadoReserva, Long> estadoCounts = ImmutableMap.copyOf(
            reservas.stream()
                .filter(r -> r.getEstado() != null)
                .collect(Collectors.groupingBy(
                    Reserva::getEstado,
                    Collectors.counting()
                ))
        );

        ReservaSummary summary = new ReservaSummary(total, avgMonto, avgDuracion, estados, estadoCounts);

        logger.info("Resumen creado: {} reservas en {} estados", total, estados.size());

        return summary;
    }

    public static class EstadoReservaStats {
        private final EstadoReserva estado;
        private final int reservaCount;
        private final double averageMonto;
        private final double averageDuracionHoras;
        private final Reserva highestMontoReserva;
        private final Reserva longestDuracionReserva;
        private final Map<String, Long> montoRangeDistribution;

        public EstadoReservaStats(EstadoReserva estado, int reservaCount, double averageMonto,
                                  double averageDuracionHoras, Reserva highestMontoReserva,
                                  Reserva longestDuracionReserva, Map<String, Long> montoRangeDistribution) {
            this.estado = estado;
            this.reservaCount = reservaCount;
            this.averageMonto = averageMonto;
            this.averageDuracionHoras = averageDuracionHoras;
            this.highestMontoReserva = highestMontoReserva;
            this.longestDuracionReserva = longestDuracionReserva;
            this.montoRangeDistribution = montoRangeDistribution;
        }

        // Getters
        public EstadoReserva getEstado() { return estado; }
        public int getReservaCount() { return reservaCount; }
        public double getAverageMonto() { return averageMonto; }
        public double getAverageDuracionHoras() { return averageDuracionHoras; }
        public Reserva getHighestMontoReserva() { return highestMontoReserva; }
        public Reserva getLongestDuracionReserva() { return longestDuracionReserva; }
        public Map<String, Long> getMontoRangeDistribution() { return montoRangeDistribution; }

        @Override
        public String toString() {
            return String.format("EstadoReservaStats{estado=%s, count=%d, avgMonto=%.2f, avgDuracion=%.1f}",
                estado, reservaCount, averageMonto, averageDuracionHoras);
        }
    }

    public static class ReservaSummary {
        private final int totalReservas;
        private final double averageMonto;
        private final double averageDuracionHoras;
        private final ImmutableSet<EstadoReserva> estados;
        private final ImmutableMap<EstadoReserva, Long> estadoCounts;

        public ReservaSummary(int totalReservas, double averageMonto, double averageDuracionHoras,
                              ImmutableSet<EstadoReserva> estados, ImmutableMap<EstadoReserva, Long> estadoCounts) {
            this.totalReservas = totalReservas;
            this.averageMonto = averageMonto;
            this.averageDuracionHoras = averageDuracionHoras;
            this.estados = estados;
            this.estadoCounts = estadoCounts;
        }

        // Getters
        public int getTotalReservas() { return totalReservas; }
        public double getAverageMonto() { return averageMonto; }
        public double getAverageDuracionHoras() { return averageDuracionHoras; }
        public ImmutableSet<EstadoReserva> getEstados() { return estados; }
        public ImmutableMap<EstadoReserva, Long> getEstadoCounts() { return estadoCounts; }

        @Override
        public String toString() {
            return String.format("ReservaSummary{total=%d, avgMonto=%.2f, avgDuracion=%.1f, estados=%d}",
                totalReservas, averageMonto, averageDuracionHoras, estados.size());
        }
    }
}
