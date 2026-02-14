package com.eraf.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main service for report generation.
 *
 * <p>Manages {@link ReportGenerator} instances and delegates report generation
 * to the appropriate generator based on the requested {@link ReportFormat}.</p>
 *
 * <p>Built-in generators (CSV, HTML) are registered automatically via
 * {@link ReportAutoConfiguration}. Additional generators (PDF, Excel) can be
 * registered programmatically or via Spring beans.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * ReportDefinition def = ReportDefinition.builder("sales", ReportFormat.CSV)
 *     .title("Sales Report")
 *     .build();
 *
 * ReportData data = ReportData.builder()
 *     .headers(List.of("Name", "Amount"))
 *     .addRow(Map.of("Name", "Item A", "Amount", 1000))
 *     .build();
 *
 * byte[] content = reportService.generate(def, data);
 * </pre>
 */
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final Map<ReportFormat, ReportGenerator> generators = new ConcurrentHashMap<>();
    private final ReportProperties properties;

    public ReportService(ReportProperties properties) {
        this.properties = properties;
    }

    /**
     * Registers a report generator for its format.
     * If a generator for the same format already exists, it will be replaced.
     *
     * @param generator the generator to register
     */
    public void registerGenerator(ReportGenerator generator) {
        generators.put(generator.getFormat(), generator);
        log.info("Registered report generator for format: {}", generator.getFormat());
    }

    /**
     * Generates a report as a byte array.
     *
     * @param definition the report definition
     * @param data       the report data
     * @return the generated report content
     * @throws ReportGenerationException if no generator is registered for the format
     *                                    or if maxRows is exceeded
     */
    public byte[] generate(ReportDefinition definition, ReportData data) {
        validateMaxRows(data);

        ReportGenerator generator = generators.get(definition.getFormat());
        if (generator == null) {
            throw new ReportGenerationException(
                    "No generator registered for format: " + definition.getFormat());
        }

        log.info("Generating report '{}' in {} format ({} rows)",
                definition.getName(), definition.getFormat(), data.getRows().size());

        return generator.generate(definition, data);
    }

    /**
     * Generates a report and writes it to a file.
     *
     * @param definition the report definition
     * @param data       the report data
     * @param outputPath the file path to write the report to
     * @throws ReportGenerationException if generation or file writing fails
     */
    public void generateToFile(ReportDefinition definition, ReportData data, Path outputPath) {
        byte[] content = generate(definition, data);

        try {
            Path parentDir = outputPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.write(outputPath, content);
            log.info("Report written to: {}", outputPath);
        } catch (IOException e) {
            throw new ReportGenerationException(
                    "Failed to write report to file: " + outputPath, e);
        }
    }

    /**
     * Returns the list of formats that have registered generators.
     */
    public List<ReportFormat> getSupportedFormats() {
        return List.copyOf(generators.keySet());
    }

    /**
     * Checks if a generator is registered for the given format.
     */
    public boolean isFormatSupported(ReportFormat format) {
        return generators.containsKey(format);
    }

    private void validateMaxRows(ReportData data) {
        int maxRows = properties.getMaxRows();
        if (maxRows > 0 && data.getRows().size() > maxRows) {
            throw new ReportGenerationException(
                    "Row count " + data.getRows().size() + " exceeds maximum allowed: " + maxRows);
        }
    }
}
