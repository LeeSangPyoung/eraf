package com.eraf.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * JasperReports PDF Generator
 *
 * JRXML 템플릿 기반 PDF 리포트 생성을 지원합니다.
 */
public class JasperReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(JasperReportGenerator.class);

    private final String templatePath;

    /**
     * @param templatePath JRXML 템플릿 파일 경로 (classpath 기준)
     */
    public JasperReportGenerator(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.PDF;
    }

    @Override
    public byte[] generate(ReportDefinition definition, ReportData data) {
        try {
            // 1. JRXML 템플릿 로드 및 컴파일
            JasperReport jasperReport = compileTemplate();

            // 2. 파라미터 준비
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", definition.getTitle());
            parameters.put("REPORT_NAME", definition.getName());

            // 사용자 정의 파라미터는 data의 metadata에서 가져옴
            if (data.getMetadata() != null) {
                parameters.putAll(data.getMetadata());
            }

            // 3. 데이터 소스 생성
            JRDataSource dataSource = createDataSource(data);

            // 4. 리포트 생성
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // 5. PDF로 export
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            log.info("JasperReports PDF generated successfully: {}", definition.getName());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate JasperReports PDF: {}", e.getMessage(), e);
            throw new ReportGenerationException("JasperReports PDF generation failed", e);
        }
    }

    /**
     * JRXML 템플릿 컴파일
     */
    private JasperReport compileTemplate() throws JRException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new ReportGenerationException("Template not found: " + templatePath);
            }

            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            return JasperCompileManager.compileReport(jasperDesign);

        } catch (Exception e) {
            throw new ReportGenerationException("Failed to compile JRXML template", e);
        }
    }

    /**
     * ReportData를 JRDataSource로 변환
     */
    private JRDataSource createDataSource(ReportData data) {
        // ReportData의 rows는 이미 List<Map<String, Object>> 형태
        return new JRBeanCollectionDataSource(data.getRows());
    }

    /**
     * Builder for JasperReportGenerator
     */
    public static class Builder {
        private String templatePath;

        public Builder templatePath(String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        public JasperReportGenerator build() {
            if (templatePath == null || templatePath.isBlank()) {
                throw new IllegalArgumentException("Template path must be specified");
            }
            return new JasperReportGenerator(templatePath);
        }
    }
}
