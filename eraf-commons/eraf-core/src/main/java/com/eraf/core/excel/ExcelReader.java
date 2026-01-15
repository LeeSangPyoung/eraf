package com.eraf.core.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Excel 읽기 유틸리티
 */
public class ExcelReader implements AutoCloseable {

    private final Workbook workbook;

    private ExcelReader(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * Excel 파일 열기
     */
    public static ExcelReader open(Path path) throws IOException {
        InputStream is = Files.newInputStream(path);
        String filename = path.getFileName().toString().toLowerCase();

        Workbook workbook;
        if (filename.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(is);
        } else if (filename.endsWith(".xls")) {
            workbook = new HSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + filename);
        }

        return new ExcelReader(workbook);
    }

    /**
     * 시트 개수 조회
     */
    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    /**
     * 시트명 목록 조회
     */
    public List<String> getSheetNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            names.add(workbook.getSheetName(i));
        }
        return names;
    }

    /**
     * 전체 데이터 읽기 (첫 번째 시트)
     */
    public List<List<Object>> readAll() {
        return readAll(0);
    }

    /**
     * 전체 데이터 읽기 (시트 인덱스 지정)
     */
    public List<List<Object>> readAll(int sheetIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        return readSheet(sheet);
    }

    /**
     * 전체 데이터 읽기 (시트명 지정)
     */
    public List<List<Object>> readAll(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("시트를 찾을 수 없습니다: " + sheetName);
        }
        return readSheet(sheet);
    }

    /**
     * 헤더 포함 Map으로 읽기 (첫 행을 키로 사용)
     */
    public List<Map<String, Object>> readAsMap() {
        return readAsMap(0);
    }

    /**
     * 헤더 포함 Map으로 읽기
     */
    public List<Map<String, Object>> readAsMap(int sheetIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        List<List<Object>> data = readSheet(sheet);

        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        // 첫 행을 헤더로 사용
        List<Object> headers = data.get(0);
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 1; i < data.size(); i++) {
            List<Object> row = data.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                String key = String.valueOf(headers.get(j));
                Object value = j < row.size() ? row.get(j) : null;
                map.put(key, value);
            }
            result.add(map);
        }

        return result;
    }

    /**
     * 특정 셀 값 읽기
     */
    public Object getCellValue(int sheetIndex, int rowIndex, int colIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        return getCellValue(cell);
    }

    private List<List<Object>> readSheet(Sheet sheet) {
        List<List<Object>> data = new ArrayList<>();

        for (Row row : sheet) {
            List<Object> rowData = new ArrayList<>();
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                rowData.add(getCellValue(cell));
            }
            data.add(rowData);
        }

        return data;
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                }
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    yield (long) value;
                }
                yield value;
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> {
                try {
                    yield cell.getNumericCellValue();
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            case BLANK -> null;
            default -> null;
        };
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
