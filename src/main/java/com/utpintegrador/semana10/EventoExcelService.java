package com.utpintegrador.semana10;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventoExcelService {
    private static final Logger logger = LoggerFactory.getLogger(EventoExcelService.class);

    // Índices de columnas
    private static final int COL_ID = 0;
    private static final int COL_TITULO = 1;
    private static final int COL_DESCRIPCION = 2;
    private static final int COL_CAPACIDAD = 3;
    private static final int COL_PRECIO = 4;
    private static final int COL_UBICACION = 5;
    private static final int COL_FECHA = 6;
    private static final int COL_ESTADO = 7;

    public List<Evento> leerEventosDesdeExcel(String filePath) throws IOException {
        logger.info("Leyendo eventos desde archivo: {}", filePath);
        List<Evento> eventos = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    extraerEventoDesdeFila(row).ifPresent(eventos::add);
                } catch (Exception e) {
                    logger.error("Error procesando fila {}: {}", i, e.getMessage());
                }
            }
        }
        logger.info("Se leyeron {} eventos desde el archivo", eventos.size());
        return eventos;
    }

    private java.util.Optional<Evento> extraerEventoDesdeFila(Row row) {
        try {
            Evento evento = new Evento();

            Cell idCell = row.getCell(COL_ID, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (idCell != null) evento.setId((long) idCell.getNumericCellValue());

            evento.setTitulo(getStringCellValue(row.getCell(COL_TITULO)));
            evento.setDescripcion(getStringCellValue(row.getCell(COL_DESCRIPCION)));
            
            Cell capacidadCell = row.getCell(COL_CAPACIDAD, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (capacidadCell != null) evento.setCapacidad((int) capacidadCell.getNumericCellValue());

            Cell precioCell = row.getCell(COL_PRECIO, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (precioCell != null) evento.setPrecioHora(precioCell.getNumericCellValue());

            evento.setUbicacion(getStringCellValue(row.getCell(COL_UBICACION)));

            Cell fechaCell = row.getCell(COL_FECHA, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (fechaCell != null && DateUtil.isCellDateFormatted(fechaCell)) {
                Date fecha = fechaCell.getDateCellValue();
                evento.setFecha(fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }

            String estadoStr = getStringCellValue(row.getCell(COL_ESTADO));
            for (Evento.Estado estado : Evento.Estado.values()) {
                if (estado.getTexto().equalsIgnoreCase(estadoStr)) {
                    evento.setEstado(estado);
                    break;
                }
            }
            return java.util.Optional.of(evento);
        } catch (Exception e) {
            logger.error("Error extrayendo evento de la fila {}: {}", row.getRowNum(), e.getMessage());
            return java.util.Optional.empty();
        }
    }

    public void escribirEventosAExcel(List<Evento> eventos, String filePath) throws IOException {
        logger.info("Escribiendo {} eventos a archivo: {}", eventos.size(), filePath);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Eventos");
            crearEncabezados(sheet, workbook);
            int rowNum = 1;
            for (Evento evento : eventos) {
                Row row = sheet.createRow(rowNum++);
                llenarFilaEvento(row, evento, workbook);
            }
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private void crearEncabezados(Sheet sheet, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Título", "Descripción", "Capacidad", "Precio por Hora", "Ubicación", "Fecha", "Estado"};
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void llenarFilaEvento(Row row, Evento evento, Workbook workbook) {
        row.createCell(COL_ID).setCellValue(evento.getId() != null ? evento.getId() : 0);
        row.createCell(COL_TITULO).setCellValue(evento.getTitulo() != null ? evento.getTitulo() : "");
        row.createCell(COL_DESCRIPCION).setCellValue(evento.getDescripcion() != null ? evento.getDescripcion() : "");
        row.createCell(COL_CAPACIDAD).setCellValue(evento.getCapacidad() != null ? evento.getCapacidad() : 0);

        Cell precioCell = row.createCell(COL_PRECIO);
        precioCell.setCellValue(evento.getPrecioHora() != null ? evento.getPrecioHora() : 0.0);
        precioCell.setCellStyle(crearEstiloMoneda(workbook));

        row.createCell(COL_UBICACION).setCellValue(evento.getUbicacion() != null ? evento.getUbicacion() : "");

        Cell fechaCell = row.createCell(COL_FECHA);
        if (evento.getFecha() != null) {
            fechaCell.setCellValue(Date.from(evento.getFecha().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            fechaCell.setCellStyle(crearEstiloFecha(workbook));
        }

        row.createCell(COL_ESTADO).setCellValue(evento.getEstado() != null ? evento.getEstado().getTexto() : "");
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.toString().trim();
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("S/ #,##0.00"));
        return style;
    }

    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));
        return style;
    }
}