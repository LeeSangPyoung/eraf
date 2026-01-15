package com.eraf.core.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * PDF 텍스트 추출 유틸리티
 */
public final class PdfTextUtils {

    private PdfTextUtils() {
    }

    /**
     * PDF 전체 텍스트 추출
     */
    public static String extractText(Path pdfPath) throws IOException {
        StringBuilder text = new StringBuilder();
        PdfReader reader = new PdfReader(pdfPath.toString());

        try {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            int pages = reader.getNumberOfPages();

            for (int i = 1; i <= pages; i++) {
                text.append(extractor.getTextFromPage(i));
                if (i < pages) {
                    text.append("\n\n");
                }
            }
        } finally {
            reader.close();
        }

        return text.toString();
    }

    /**
     * 특정 페이지 텍스트 추출
     */
    public static String extractText(Path pdfPath, int page) throws IOException {
        PdfReader reader = new PdfReader(pdfPath.toString());

        try {
            if (page < 1 || page > reader.getNumberOfPages()) {
                throw new IllegalArgumentException("유효하지 않은 페이지: " + page);
            }

            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            return extractor.getTextFromPage(page);
        } finally {
            reader.close();
        }
    }

    /**
     * PDF 페이지 범위 텍스트 추출
     */
    public static String extractText(Path pdfPath, int startPage, int endPage) throws IOException {
        StringBuilder text = new StringBuilder();
        PdfReader reader = new PdfReader(pdfPath.toString());

        try {
            int totalPages = reader.getNumberOfPages();
            if (startPage < 1 || startPage > totalPages) {
                throw new IllegalArgumentException("유효하지 않은 시작 페이지: " + startPage);
            }
            if (endPage < startPage || endPage > totalPages) {
                throw new IllegalArgumentException("유효하지 않은 끝 페이지: " + endPage);
            }

            PdfTextExtractor extractor = new PdfTextExtractor(reader);

            for (int i = startPage; i <= endPage; i++) {
                text.append(extractor.getTextFromPage(i));
                if (i < endPage) {
                    text.append("\n\n");
                }
            }
        } finally {
            reader.close();
        }

        return text.toString();
    }
}
