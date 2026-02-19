package com.eraf.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * ERAF PDF Auto-Configuration
 *
 * <p>PDF 생성, 병합, 분할, 텍스트 추출 기능을 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>HTML to PDF 변환 (PdfGenerator)</li>
 *   <li>PDF 파일 병합 (PdfMerger)</li>
 *   <li>PDF 파일 분할 (PdfSplitter)</li>
 *   <li>PDF 텍스트 추출 (PdfTextUtils)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // PDF 생성 (HTML to PDF)
 * PdfGenerator.fromHtml(html, outputPath);
 * byte[] pdf = PdfGenerator.fromHtmlBytes(html);
 *
 * // PDF 병합
 * PdfMerger.merge(List.of(pdf1, pdf2, pdf3), outputPath);
 *
 * // PDF 분할
 * List&lt;byte[]&gt; pages = PdfSplitter.splitByPage(pdfBytes);
 * PdfSplitter.splitByRange(inputPath, outputPath, 1, 10);
 *
 * // 텍스트 추출
 * String text = PdfTextUtils.extractText(pdfBytes);
 * </pre>
 *
 * <p>Note: PDF 유틸리티들은 static 메서드로 제공되므로 직접 호출하여 사용합니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.lowagie.text.pdf.PdfWriter")
public class ErafPdfAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafPdfAutoConfiguration.class);

    public ErafPdfAutoConfiguration() {
        log.info("ERAF PDF module enabled - PDF generation, merge, split utilities available");
        log.debug("Use PdfGenerator, PdfMerger, PdfSplitter, PdfTextUtils static methods");
    }
}
