package semana12;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelService.class);

    // Índices de columnas en el archivo Excel
    private static final int COL_ID            = 0;
    private static final int COL_USUARIO_ID    = 1;
    private static final int COL_LOCAL_ID      = 2;
    private static final int COL_FECHA_INICIO  = 3;
    private static final int COL_FECHA_FIN     = 4;
    private static final int COL_MONTO         = 5;
    private static final int COL_ESTADO        = 6;

    /**
     * Lee reservas desde un archivo Excel
     */
    public List<Reserva> readReservasFromExcel(String filePath) throws IOException {
        logger.info("Leyendo reservas desde archivo: {}", filePath);

        List<Reserva> reservas = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Reserva r = extractReservaFromRow(row);
                    if (r != null) reservas.add(r);
                } catch (Exception e) {
                    logger.error("Error procesando fila {}: {}", i, e.getMessage());
                }
            }
        }

        logger.info("Se leyeron {} reservas", reservas.size());
        return reservas;
    }

    /**
     * Extrae una reserva de una fila del Excel
     */
    private Reserva extractReservaFromRow(Row row) {
        Reserva reserva = new Reserva();

        // ID (UUID guardado como cadena)
        Cell idCell = row.getCell(COL_ID);
        if (idCell != null) {
            String idStr = getCellValueAsString(idCell);
            if (!idStr.isEmpty()) {
                reserva.setIdReserva(UUID.fromString(idStr));
            }
        }

        // Usuario ID
        Cell userCell = row.getCell(COL_USUARIO_ID);
        if (userCell != null && userCell.getCellType() == CellType.NUMERIC) {
            reserva.setUsuarioId((int)userCell.getNumericCellValue());
        }

        // Local ID
        Cell localCell = row.getCell(COL_LOCAL_ID);
        if (localCell != null && localCell.getCellType() == CellType.NUMERIC) {
            reserva.setLocalId((int)localCell.getNumericCellValue());
        }

        // Fecha de inicio
        Cell startCell = row.getCell(COL_FECHA_INICIO);
        if (startCell != null && DateUtil.isCellDateFormatted(startCell)) {
            Date d = startCell.getDateCellValue();
            reserva.setFechaInicio(LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()));
        }

        // Fecha de fin
        Cell endCell = row.getCell(COL_FECHA_FIN);
        if (endCell != null && DateUtil.isCellDateFormatted(endCell)) {
            Date d = endCell.getDateCellValue();
            reserva.setFechaFin(LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()));
        }

        // Monto
        Cell montoCell = row.getCell(COL_MONTO);
        if (montoCell != null && montoCell.getCellType() == CellType.NUMERIC) {
            reserva.setMonto(montoCell.getNumericCellValue());
        }

        // Estado
        String estadoStr = getCellValueAsString(row.getCell(COL_ESTADO));
        if (!estadoStr.isEmpty()) {
            reserva.setEstado(EstadoReserva.valueOf(estadoStr));
        }

        return reserva;
    }

    /**
     * Obtiene el valor de una celda como String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default:      return "";
        }
    }

    /**
     * Escribe reservas a un archivo Excel
     */
    public void writeReservasToExcel(List<Reserva> reservas, String filePath) throws IOException {
        logger.info("Escribiendo {} reservas a archivo: {}", reservas.size(), filePath);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reservas");

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle   = createDataStyle(workbook);
            CellStyle dateStyle   = createDateTimeStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Encabezados
            Row header = sheet.createRow(0);
            String[] headers = {
                "ID",
                "UsuarioId",
                "LocalId",
                "FechaInicio",
                "FechaFin",
                "Monto",
                "Estado"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (Reserva r : reservas) {
                Row row = sheet.createRow(rowNum++);

                // ID
                Cell c0 = row.createCell(COL_ID);
                c0.setCellValue(r.getIdReserva().toString());
                c0.setCellStyle(dataStyle);

                // UsuarioId
                Cell c1 = row.createCell(COL_USUARIO_ID);
                c1.setCellValue(r.getUsuarioId() != null ? r.getUsuarioId() : 0);
                c1.setCellStyle(dataStyle);

                // LocalId
                Cell c2 = row.createCell(COL_LOCAL_ID);
                c2.setCellValue(r.getLocalId() != null ? r.getLocalId() : 0);
                c2.setCellStyle(dataStyle);

                // FechaInicio
                Cell c3 = row.createCell(COL_FECHA_INICIO);
                if (r.getFechaInicio() != null) {
                    Instant inst = r.getFechaInicio().atZone(ZoneId.systemDefault()).toInstant();
                    c3.setCellValue(Date.from(inst));
                    c3.setCellStyle(dateStyle);
                } else {
                    c3.setCellValue("");
                    c3.setCellStyle(dataStyle);
                }

                // FechaFin
                Cell c4 = row.createCell(COL_FECHA_FIN);
                if (r.getFechaFin() != null) {
                    Instant inst = r.getFechaFin().atZone(ZoneId.systemDefault()).toInstant();
                    c4.setCellValue(Date.from(inst));
                    c4.setCellStyle(dateStyle);
                } else {
                    c4.setCellValue("");
                    c4.setCellStyle(dataStyle);
                }

                // Monto
                Cell c5 = row.createCell(COL_MONTO);
                if (r.getMonto() != null) {
                    c5.setCellValue(r.getMonto());
                    c5.setCellStyle(currencyStyle);
                } else {
                    c5.setCellValue(0.0);
                    c5.setCellStyle(dataStyle);
                }

                // Estado
                Cell c6 = row.createCell(COL_ESTADO);
                c6.setCellValue(r.getEstado() != null ? r.getEstado().name() : "");
                c6.setCellStyle(dataStyle);
            }

            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short)12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private CellStyle createDateTimeStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        CreationHelper helper = wb.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        DataFormat df = wb.createDataFormat();
        style.setDataFormat(df.getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
