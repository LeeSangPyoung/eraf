package com.eraf.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * HTML table report generator.
 *
 * <p>Generates a self-contained HTML document with an inline-styled table.
 * The output includes a title section, data table with headers, and a
 * footer with metadata information.</p>
 */
public class HtmlReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(HtmlReportGenerator.class);

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.HTML;
    }

    @Override
    public byte[] generate(ReportDefinition definition, ReportData data) {
        log.debug("Generating HTML report: {}", definition.getName());

        StringBuilder html = new StringBuilder();

        // HTML header
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ko\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <title>").append(escapeHtml(getTitle(definition))).append("</title>\n");
        html.append("  <style>\n");
        html.append(getDefaultStyle());
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Title
        String title = getTitle(definition);
        html.append("  <h1>").append(escapeHtml(title)).append("</h1>\n");

        if (definition.getDescription() != null && !definition.getDescription().isBlank()) {
            html.append("  <p class=\"description\">").append(escapeHtml(definition.getDescription())).append("</p>\n");
        }

        // Table
        List<String> headers = data.getHeaders();
        html.append("  <table>\n");

        // Table header
        html.append("    <thead>\n");
        html.append("      <tr>\n");
        for (String header : headers) {
            html.append("        <th>").append(escapeHtml(header)).append("</th>\n");
        }
        html.append("      </tr>\n");
        html.append("    </thead>\n");

        // Table body
        html.append("    <tbody>\n");
        if (data.isEmpty()) {
            html.append("      <tr><td colspan=\"").append(headers.size())
                    .append("\" class=\"empty\">No data</td></tr>\n");
        } else {
            for (Map<String, Object> row : data.getRows()) {
                html.append("      <tr>\n");
                for (String header : headers) {
                    Object value = row.get(header);
                    String cellValue = value != null ? String.valueOf(value) : "";
                    html.append("        <td>").append(escapeHtml(cellValue)).append("</td>\n");
                }
                html.append("      </tr>\n");
            }
        }
        html.append("    </tbody>\n");
        html.append("  </table>\n");

        // Footer
        html.append("  <div class=\"footer\">\n");
        html.append("    <p>Total rows: ").append(data.getRows().size()).append("</p>\n");
        if (data.getTotalCount() > 0) {
            html.append("    <p>Total count: ").append(data.getTotalCount()).append("</p>\n");
        }
        for (Map.Entry<String, Object> entry : data.getMetadata().entrySet()) {
            html.append("    <p>").append(escapeHtml(entry.getKey())).append(": ")
                    .append(escapeHtml(String.valueOf(entry.getValue()))).append("</p>\n");
        }
        html.append("    <p>Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("</p>\n");
        html.append("  </div>\n");

        html.append("</body>\n");
        html.append("</html>\n");

        log.debug("HTML report generated: {} rows", data.getRows().size());
        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String getTitle(ReportDefinition definition) {
        if (definition.getTitle() != null && !definition.getTitle().isBlank()) {
            return definition.getTitle();
        }
        return definition.getName();
    }

    private String getDefaultStyle() {
        return """
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; color: #333; }
                    h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
                    .description { color: #7f8c8d; font-style: italic; }
                    table { border-collapse: collapse; width: 100%; margin-top: 20px; }
                    th { background-color: #3498db; color: white; padding: 12px 8px; text-align: left; }
                    td { padding: 10px 8px; border-bottom: 1px solid #ddd; }
                    tr:nth-child(even) { background-color: #f8f9fa; }
                    tr:hover { background-color: #e8f4f8; }
                    .empty { text-align: center; color: #999; padding: 20px; }
                    .footer { margin-top: 20px; padding-top: 10px; border-top: 1px solid #ddd; color: #7f8c8d; font-size: 0.9em; }
                """;
    }

    /**
     * Escapes special HTML characters to prevent XSS.
     */
    static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
