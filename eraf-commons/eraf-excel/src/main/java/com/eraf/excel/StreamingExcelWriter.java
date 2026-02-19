package com.eraf.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
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
 *
 * <p>SXSSF(Streaming Usermodel API) 기반으로 메모리를 효율적으로 사용합니다.
 * 지정된 행 수만 메모리에 유지하고 나머지는 임시 파일로 플러시합니다.
 *
 * <p>사용 예:
 * <pre>{@code
 * try (StreamingExcelWriter writer = new StreamingExcelWriter()) {
 *     writer.writeStreaming("시트명", headers, () -> repository.streamAll(),
 *         entity -> List.of(entity.getId(), entity.getName()),
 *         totalCount,
 *         progress -> log.info("진행률: {}%", progress.getPercent()));
 *     writer.write(response.getOutputStream());
 * }
 * }</pre>
 */
public class StreamingExcelWriter implements AutoCloseable {

    private static final int DEFAULT_ROW_ACCESS_WINDOW_SIZE = 100;
    private static final int FLUSH_INTERVAL = 1000;

    /**
     * 스트리밍 진행 상태 콜백 인터페이스
     */
    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(Progress progress);
    }

    /**
     * 스트리밍 진행 상태 정보
     */
    public static class Progress {
        private final long writtenRows;
        private final long totalRows;
        private final String sheetName;

        public Progress(long writtenRows, long totalRows, String sheetName) {
            this.writtenRows = writtenRows;
            this.totalRows = totalRows;
            this.sheetName = sheetName;
        }

        public long getWrittenRows() { return writtenRows; }
        public long getTotalRows() { return totalRows; }
        public String getSheetName() { return sheetName; }

        /** 0~100 사이의 진행률 (totalRows가 0 이하이면 -1 반환) */
        public int getPercent() {
            if (totalRows <= 0) return -1;
            return (int) Math.min(100, writtenRows * 100 / totalRows);
        }

        @Override
        public String toString() {
            return "Progress{sheet='" + sheetName + "', written=" + writtenRows
                    + (totalRows > 0 ? "/" + totalRows + " (" + getPercent() + "%)" : "") + "}";
        }
    }

    private final SXSSFWorkbook workbook;
    private CellStyle headerStyle;
    private CellStyle dataStyle;
    private CellStyle dateStyle;
    private CellStyle dateTimeStyle;

    public StreamingExcelWriter() {
        this(DEFAULT_ROW_ACCESS_WINDOW_SIZE);
    }

    public StreamingExcelWriter(int rowAccessWindowSize) {
        this.workbook = new SXSSFWorkbook(rowAccessWindowSize);
        workbook.setCompressTempFiles(true);
        initStyles();
    }

    private void initStyles() {
        CreationHelper createHelper = workbook.getCreationHelper();

        // 헤더 스타일
        headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // 데이터 스타일
        dataStyle = workbook.createCellStyle();

        // 날짜 스타일 (yyyy-MM-dd)
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // 날짜+시간 스타일 (yyyy-MM-dd HH:mm:ss)
        dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
    }

    /**
     * 대용량 데이터 스트리밍 쓰기
     *
     * @param sheetName    시트명
     * @param headers      헤더 목록
     * @param dataSupplier 데이터 공급자 (Iterator 반환)
     * @param rowMapper    행 데이터 변환기
     * @return 실제 작성된 데이터 행 수
     */
    public <T> long writeStreaming(String sheetName,
                                   List<String> headers,
                                   Supplier<Iterator<T>> dataSupplier,
                                   Function<T, List<Object>> rowMapper) {
        return writeStreaming(sheetName, headers, dataSupplier, rowMapper, -1, null);
    }

    /**
     * 대용량 데이터 스트리밍 쓰기 (진행 상태 콜백 포함)
     *
     * @param sheetName    시트명
     * @param headers      헤더 목록
     * @param dataSupplier 데이터 공급자 (Iterator 반환)
     * @param rowMapper    행 데이터 변환기
     * @param totalRows    전체 데이터 건수 (-1이면 알 수 없음)
     * @param callback     진행 상태 콜백 (null이면 무시). FLUSH_INTERVAL(1000)행마다 호출됨
     * @return 실제 작성된 데이터 행 수
     */
    public <T> long writeStreaming(String sheetName,
                                   List<String> headers,
                                   Supplier<Iterator<T>> dataSupplier,
                                   Function<T, List<Object>> rowMapper,
                                   long totalRows,
                                   ProgressCallback callback) {
        SXSSFSheet sheet = workbook.createSheet(sheetName);

        // 헤더 기반 컬럼 초기 너비 설정 (SXSSF는 autoSizeColumn이 플러시 후 동작 안 함)
        for (int i = 0; i < headers.size(); i++) {
            int width = Math.min(headers.get(i).length() * 300 + 512, 15000);
            sheet.setColumnWidth(i, width);
        }

        // 헤더 쓰기
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 데이터 스트리밍
        Iterator<T> iterator = dataSupplier.get();
        long rowNum = 1;

        while (iterator.hasNext()) {
            T item = iterator.next();
            List<Object> rowData = rowMapper.apply(item);

            Row row = sheet.createRow((int) rowNum);
            for (int i = 0; i < rowData.size(); i++) {
                Cell cell = row.createCell(i);
                setCellValue(cell, rowData.get(i));
            }
            rowNum++;

            // 주기적으로 행 플러시 (메모리 관리)
            if (rowNum % FLUSH_INTERVAL == 0) {
                try {
                    sheet.flushRows(FLUSH_INTERVAL);
                } catch (IOException e) {
                    throw new UncheckedIOException("Row flush failed at row " + rowNum, e);
                }
                // 진행 상태 콜백 호출
                if (callback != null) {
                    callback.onProgress(new Progress(rowNum - 1, totalRows, sheetName));
                }
            }
        }

        // 마지막 진행 상태 콜백
        if (callback != null) {
            callback.onProgress(new Progress(rowNum - 1, totalRows, sheetName));
        }

        return rowNum - 1;
    }

    /**
     * List&lt;Map&gt; 데이터 스트리밍 쓰기
     */
    public long writeFromMapsStreaming(String sheetName,
                                      List<String> headers,
                                      Supplier<Iterator<Map<String, Object>>> dataSupplier) {
        return writeStreaming(sheetName, headers, dataSupplier,
                map -> headers.stream().map(map::get).toList());
    }

    /**
     * List&lt;Map&gt; 데이터 스트리밍 쓰기 (진행 상태 콜백 포함)
     */
    public long writeFromMapsStreaming(String sheetName,
                                      List<String> headers,
                                      Supplier<Iterator<Map<String, Object>>> dataSupplier,
                                      long totalRows,
                                      ProgressCallback callback) {
        return writeStreaming(sheetName, headers, dataSupplier,
                map -> headers.stream().map(map::get).toList(), totalRows, callback);
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
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
            cell.setCellStyle(dataStyle);
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
            cell.setCellStyle(dataStyle);
        } else if (value instanceof LocalDate ld) {
            cell.setCellValue(ld);
            cell.setCellStyle(dateStyle);
        } else if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt);
            cell.setCellStyle(dateTimeStyle);
        } else {
            cell.setCellValue(String.valueOf(value));
            cell.setCellStyle(dataStyle);
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
        workbook.dispose(); // 임시 파일 정리
    }
}
