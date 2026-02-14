package com.eraf.report;

/**
 * Interface for format-specific report generators.
 *
 * <p>Implementations handle rendering {@link ReportData} into a specific
 * {@link ReportFormat} (e.g., CSV, HTML, PDF, Excel).</p>
 *
 * <p>Register custom generators with {@link ReportService#registerGenerator(ReportGenerator)}
 * to add support for additional formats or override built-in generators.</p>
 */
public interface ReportGenerator {

    /**
     * Returns the format this generator produces.
     */
    ReportFormat getFormat();

    /**
     * Generates a report as a byte array.
     *
     * @param definition the report definition (name, title, parameters)
     * @param data       the data to render
     * @return the generated report content as bytes
     */
    byte[] generate(ReportDefinition definition, ReportData data);
}
