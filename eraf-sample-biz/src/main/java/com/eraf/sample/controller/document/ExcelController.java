package com.eraf.sample.controller.document;

import com.eraf.excel.ExcelReader;
import com.eraf.excel.ExcelWriter;
import com.eraf.excel.StreamingExcelWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Excel 기능 시연 컨트롤러
 * #1 Excel 다운로드
 * #2 Excel 업로드/파싱
 * #3 Excel 템플릿 기반 생성
 * #4 대용량 Excel 스트리밍
 */
@Slf4j
@Controller
@RequestMapping("/document/excel")
public class ExcelController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("menu", "excel");
        model.addAttribute("title", "Excel - ERAF Sample");
        return "document/excel";
    }

    /**
     * #1 Excel 다운로드 - 샘플 데이터
     */
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        // 샘플 데이터 생성
        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("ID", "Name", "Email", "Department", "JoinDate"));
        data.add(Arrays.asList(1, "John Doe", "john@example.com", "Development", LocalDate.of(2023, 1, 15)));
        data.add(Arrays.asList(2, "Jane Smith", "jane@example.com", "Design", LocalDate.of(2023, 3, 20)));
        data.add(Arrays.asList(3, "Bob Johnson", "bob@example.com", "Marketing", LocalDate.of(2023, 5, 10)));
        data.add(Arrays.asList(4, "Alice Brown", "alice@example.com", "HR", LocalDate.of(2023, 7, 1)));
        data.add(Arrays.asList(5, "Charlie Wilson", "charlie@example.com", "Development", LocalDate.of(2023, 9, 15)));

        // Response 설정
        String filename = URLEncoder.encode("employees.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (ExcelWriter writer = new ExcelWriter()) {
            writer.write("Employees", data);
            writer.write(response.getOutputStream());
        }

        log.info("Excel download completed: employees.xlsx");
    }

    /**
     * #2 Excel 업로드/파싱
     */
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "File is empty");
            return result;
        }

        // 임시 파일로 저장
        Path tempFile = Files.createTempFile("excel_", ".xlsx");
        file.transferTo(tempFile.toFile());

        try (ExcelReader reader = ExcelReader.open(tempFile)) {
            List<String> sheetNames = reader.getSheetNames();
            List<Map<String, Object>> data = reader.readAsMap();

            result.put("success", true);
            result.put("fileName", file.getOriginalFilename());
            result.put("sheetCount", reader.getSheetCount());
            result.put("sheetNames", sheetNames);
            result.put("rowCount", data.size());
            result.put("data", data.size() > 100 ? data.subList(0, 100) : data);

            log.info("Excel upload completed: {} - {} rows", file.getOriginalFilename(), data.size());
        } finally {
            Files.deleteIfExists(tempFile);
        }

        return result;
    }

    /**
     * #3 Excel 템플릿 기반 생성
     */
    @GetMapping("/template")
    public void downloadTemplate(
            @RequestParam(defaultValue = "report") String type,
            HttpServletResponse response) throws IOException {

        String filename;
        List<List<Object>> data = new ArrayList<>();

        switch (type) {
            case "invoice" -> {
                filename = "invoice_template.xlsx";
                data.add(Arrays.asList("Invoice No", "Customer", "Product", "Quantity", "Unit Price", "Total"));
                data.add(Arrays.asList("INV-001", "ACME Corp", "Widget A", 10, 100.00, 1000.00));
                data.add(Arrays.asList("INV-002", "ACME Corp", "Widget B", 5, 200.00, 1000.00));
                data.add(Arrays.asList("", "", "", "", "Grand Total", 2000.00));
            }
            case "attendance" -> {
                filename = "attendance_template.xlsx";
                data.add(Arrays.asList("Employee ID", "Name", "Date", "Check In", "Check Out", "Hours"));
                for (int i = 1; i <= 10; i++) {
                    data.add(Arrays.asList("EMP-" + String.format("%03d", i), "Employee " + i, LocalDate.now(), "09:00", "18:00", 8));
                }
            }
            default -> {
                filename = "report_template.xlsx";
                data.add(Arrays.asList("Category", "Q1", "Q2", "Q3", "Q4", "Total"));
                data.add(Arrays.asList("Sales", 1000, 1200, 1500, 1800, 5500));
                data.add(Arrays.asList("Marketing", 500, 600, 700, 800, 2600));
                data.add(Arrays.asList("Development", 2000, 2200, 2400, 2600, 9200));
            }
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");

        try (ExcelWriter writer = new ExcelWriter()) {
            writer.write(type.toUpperCase(), data);
            writer.write(response.getOutputStream());
        }

        log.info("Excel template download: {}", filename);
    }

    /**
     * #4 대용량 Excel 스트리밍
     */
    @GetMapping("/streaming")
    public void downloadStreaming(
            @RequestParam(defaultValue = "10000") int rows,
            HttpServletResponse response) throws IOException {

        if (rows > 100000) rows = 100000; // 최대 10만 행 제한
        final int totalRows = rows;

        String filename = URLEncoder.encode("large_data_" + rows + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        Random random = new Random();

        try (StreamingExcelWriter writer = new StreamingExcelWriter()) {
            // 헤더
            List<String> headers = Arrays.asList("ID", "UUID", "Name", "Email", "Amount", "CreatedAt");

            // 스트리밍 방식으로 대용량 데이터 쓰기
            writer.writeStreaming(
                    "LargeData",
                    headers,
                    () -> java.util.stream.IntStream.rangeClosed(1, totalRows).iterator(),
                    i -> Arrays.asList(
                            i,
                            UUID.randomUUID().toString(),
                            "User " + i,
                            "user" + i + "@example.com",
                            Math.round(random.nextDouble() * 10000) / 100.0,
                            LocalDate.now().minusDays(random.nextInt(365))
                    )
            );

            writer.write(response.getOutputStream());
        }

        log.info("Streaming Excel download completed: {} rows", rows);
    }

    /**
     * Excel 다운로드 (JSON 데이터 기반)
     */
    @PostMapping("/download/custom")
    public void downloadCustom(
            @RequestBody Map<String, Object> request,
            HttpServletResponse response) throws IOException {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) request.get("data");
        String filename = (String) request.getOrDefault("filename", "export.xlsx");
        String sheetName = (String) request.getOrDefault("sheetName", "Sheet1");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"");

        try (ExcelWriter writer = new ExcelWriter()) {
            writer.writeFromMaps(sheetName, data);
            writer.write(response.getOutputStream());
        }
    }
}
