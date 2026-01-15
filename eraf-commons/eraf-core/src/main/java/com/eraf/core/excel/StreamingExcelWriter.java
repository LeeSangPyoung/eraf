package com.eraf.core.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 대용량 Excel 스트리밍 쓰기 (100만건+ 지원)
 */
public class StreamingExcelWriter implements AutoCloseable {

    private static final int DEFAULT_ROW_ACCESS_WINDOW_SIZE = 100;

    private final SXSSFWorkbook workbook;
    private CellStyle headerStyle;
    private CellStyle dataStyle;

    public StreamingExcelWriter() {
        this(DEFAULT_ROW_ACCESS_WINDOW_SIZE);
    }

    public StreamingExcelWriter(int rowAccessWindowSize) {
        this.workbook = new SXSSFWorkbook(rowAccessWindowSize);
        workbook.setCompressTempFiles(true);
        initStyles();
    }

    private void initStyles() {
        // 헤더 스타일
        headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 데이터 스타일
        dataStyle = workbook.createCellStyle();
    }

    /**
     * 대용량 데이터 스트리밍 쓰기
     *
     * @param sheetName 시트명
     * @param headers   헤더 목록
     * @param dataSupplier 데이터 공급자 (Iterator 반환)
     * @param rowMapper 행 데이터 변환기
     */
    public <T> void writeStreaming(String sheetName,
                                    List<String> headers,
                                    Supplier<Iterator<T>> dataSupplier,
                                    Function<T, List<Object>> rowMapper) {
        SXSSFSheet sheet = workbook.createSheet(sheetName);

        // 헤더 쓰기
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 데이터 스트리밍
        Iterator<T> iterator = dataSupplier.get();
        int rowNum = 1;

        while (iterator.hasNext()) {
            T item = iterator.next();
            List<Object> rowData = rowMapper.apply(item);

            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < rowData.size(); i++) {
                Cell cell = row.createCell(i);
                setCellValue(cell, rowData.get(i));
            }

            // 주기적으로 행 플러시 (메모리 관리)
            if (rowNum % 10000 == 0) {
                try {
                    sheet.flushRows(10000);
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * List<Map> 데이터 스트리밍 쓰기
     */
    public void writeFromMapsStreaming(String sheetName,
                                        List<String> headers,
                                        Supplier<Iterator<Map<String, Object>>> dataSupplier) {
        writeStreaming(sheetName, headers, dataSupplier,
                map -> headers.stream().map(map::get).toList());
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
        } else if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
        workbook.dispose(); // 임시 파일 정리
    }
}
