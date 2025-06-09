package com.utpintegrador.semana10;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    public Multimap<String, Evento> agruparPorUbicacion(List<Evento> eventos) {
        ImmutableListMultimap.Builder<String, Evento> builder = ImmutableListMultimap.builder();

        eventos.forEach(evento -> {
            if (evento.getUbicacion() != null) {
                builder.put(evento.getUbicacion(), evento);
            }
        });

        return builder.build();
    }

    public Map<String, Long> contarPorEstado(List<Evento> eventos) {
        return eventos.stream()
            .filter(evento -> evento.getEstado() != null)
            .collect(Collectors.groupingBy(
                evento -> evento.getEstado().getTexto(),
                Collectors.counting()
            ));
    }

    public Map<String, Double> precioPromedioPorUbicacion(List<Evento> eventos) {
        return eventos.stream()
            .filter(evento -> evento.getUbicacion() != null && evento.getPrecioHora() != null)
            .collect(Collectors.groupingBy(
                Evento::getUbicacion,
                Collectors.averagingDouble(Evento::getPrecioHora)
            ));
    }

    public ResumenEventos generarResumen(List<Evento> eventos) {
        int total = eventos.size();

        double precioPromedio = eventos.stream()
            .filter(e -> e.getPrecioHora() != null)
            .mapToDouble(Evento::getPrecioHora)
            .average()
            .orElse(0.0);

        int capacidadTotal = eventos.stream()
            .filter(e -> e.getCapacidad() != null)
            .mapToInt(Evento::getCapacidad)
            .sum();

        Set<String> ubicaciones = eventos.stream()
            .map(Evento::getUbicacion)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return new ResumenEventos(total, precioPromedio, capacidadTotal, ubicaciones);
    }

    public static class ResumenEventos {
        private final int totalEventos;
        private final double precioPromedio;
        private final int capacidadTotal;
        private final Set<String> ubicaciones;

        public ResumenEventos(int totalEventos, double precioPromedio,
                            int capacidadTotal, Set<String> ubicaciones) {
            this.totalEventos = totalEventos;
            this.precioPromedio = precioPromedio;
            this.capacidadTotal = capacidadTotal;
            this.ubicaciones = ubicaciones;
        }

        // Getters
        public int getTotalEventos() { return totalEventos; }
        public double getPrecioPromedio() { return precioPromedio; }
        public int getCapacidadTotal() { return capacidadTotal; }
        public Set<String> getUbicaciones() { return ubicaciones; }
    }
}