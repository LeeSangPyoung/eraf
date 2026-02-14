package com.eraf.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CSV format report generator.
 *
 * <p>Generates RFC 4180 compliant CSV with configurable delimiter and quote character.
 * Includes UTF-8 BOM by default for proper encoding detection in Microsoft Excel.</p>
 *
 * <p>Configuration example:</p>
 * <pre>
 * CsvReportGenerator generator = new CsvReportGenerator();
 * generator.setDelimiter(';');
 * generator.setIncludeBom(true);
 * </pre>
 */
public class CsvReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(CsvReportGenerator.class);

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private char delimiter = ',';
    private char quoteChar = '"';
    private boolean includeBom = true;

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.CSV;
    }

    @Override
    public byte[] generate(ReportDefinition definition, ReportData data) {
        log.debug("Generating CSV report: {}", definition.getName());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (includeBom) {
                baos.write(UTF8_BOM);
            }

            try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
                List<String> headers = data.getHeaders();

                // Write header row
                writeRow(writer, headers);

                // Write data rows
                for (Map<String, Object> row : data.getRows()) {
                    writeDataRow(writer, headers, row);
                }

                writer.flush();
            }

            log.debug("CSV report generated: {} rows", data.getRows().size());
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate CSV report: " + definition.getName(), e);
        }
    }

    private void writeRow(Writer writer, List<String> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                writer.write(delimiter);
            }
            writeField(writer, values.get(i));
        }
        writer.write("\r\n");
    }

    private void writeDataRow(Writer writer, List<String> headers, Map<String, Object> row) throws IOException {
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) {
                writer.write(delimiter);
            }
            Object value = row.get(headers.get(i));
            writeField(writer, value != null ? String.valueOf(value) : "");
        }
        writer.write("\r\n");
    }

    private void writeField(Writer writer, String value) throws IOException {
        if (value == null) {
            return;
        }

        boolean needsQuoting = value.indexOf(delimiter) >= 0
                || value.indexOf(quoteChar) >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;

        if (needsQuoting) {
            writer.write(quoteChar);
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == quoteChar) {
                    writer.write(quoteChar);
                }
                writer.write(c);
            }
            writer.write(quoteChar);
        } else {
            writer.write(value);
        }
    }

    // Configuration methods

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public void setQuoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
    }

    public boolean isIncludeBom() {
        return includeBom;
    }

    public void setIncludeBom(boolean includeBom) {
        this.includeBom = includeBom;
    }
}
