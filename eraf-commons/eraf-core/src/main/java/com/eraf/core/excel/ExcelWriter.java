package com.eraf.core.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Excel 쓰기 유틸리티
 */
public class ExcelWriter implements AutoCloseable {

    private final Workbook workbook;
    private CellStyle headerStyle;
    private CellStyle dataStyle;
    private CellStyle dateStyle;

    public ExcelWriter() {
        this.workbook = new XSSFWorkbook();
        initStyles();
    }

    private void initStyles() {
        // 헤더 스타일
        headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 데이터 스타일
        dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // 날짜 스타일
        dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataStyle);
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
    }

    /**
     * 시트 생성
     */
    public Sheet createSheet(String name) {
        return workbook.createSheet(name);
    }

    /**
     * 데이터 쓰기 (List<List>)
     */
    public void write(String sheetName, List<List<Object>> data) {
        write(sheetName, data, true);
    }

    /**
     * 데이터 쓰기 (헤더 스타일 적용 옵션)
     */
    public void write(String sheetName, List<List<Object>> data, boolean hasHeader) {
        Sheet sheet = workbook.createSheet(sheetName);

        for (int i = 0; i < data.size(); i++) {
            List<Object> rowData = data.get(i);
            Row row = sheet.createRow(i);

            for (int j = 0; j < rowData.size(); j++) {
                Cell cell = row.createCell(j);
                setCellValue(cell, rowData.get(j));

                if (hasHeader && i == 0) {
                    cell.setCellStyle(headerStyle);
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }

        // 열 너비 자동 조정
        if (!data.isEmpty()) {
            for (int i = 0; i < data.get(0).size(); i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    /**
     * 데이터 쓰기 (List<Map>)
     */
    public void writeFromMaps(String sheetName, List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            workbook.createSheet(sheetName);
            return;
        }

        Sheet sheet = workbook.createSheet(sheetName);

        // 헤더 (Map 키 순서)
        Map<String, Object> firstRow = data.get(0);
        String[] headers = firstRow.keySet().toArray(new String[0]);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, Object> rowData = data.get(i);

            for (int j = 0; j < headers.length; j++) {
                Cell cell = row.createCell(j);
                setCellValue(cell, rowData.get(headers[j]));
                cell.setCellStyle(dataStyle);
            }
        }

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 파일로 저장
     */
    public void save(Path path) throws IOException {
        try (OutputStream os = Files.newOutputStream(path)) {
            workbook.write(os);
        }
    }

    /**
     * OutputStream으로 출력
     */
    public void write(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String s) {
            cell.setCellValue(s);
        } else if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof LocalDate ld) {
            cell.setCellValue(ld);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
