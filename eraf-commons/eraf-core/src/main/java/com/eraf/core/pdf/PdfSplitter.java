package com.eraf.core.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF 분리 유틸리티
 */
public final class PdfSplitter {

    private PdfSplitter() {
    }

    /**
     * PDF 페이지 범위 추출
     *
     * @param source     원본 PDF
     * @param outputPath 출력 PDF
     * @param startPage  시작 페이지 (1부터)
     * @param endPage    끝 페이지 (포함)
     */
    public static void extractPages(Path source, Path outputPath, int startPage, int endPage) throws IOException {
        PdfReader reader = new PdfReader(source.toString());
        int totalPages = reader.getNumberOfPages();

        if (startPage < 1 || startPage > totalPages) {
            throw new IllegalArgumentException("시작 페이지가 유효하지 않습니다: " + startPage);
        }
        if (endPage < startPage || endPage > totalPages) {
            throw new IllegalArgumentException("끝 페이지가 유효하지 않습니다: " + endPage);
        }

        Document document = new Document();
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfCopy copy = new PdfCopy(document, os);
            document.open();

            for (int i = startPage; i <= endPage; i++) {
                copy.addPage(copy.getImportedPage(reader, i));
            }
        } catch (Exception e) {
            throw new IOException("PDF 분리 실패", e);
        } finally {
            document.close();
            reader.close();
        }
    }

    /**
     * PDF를 각 페이지별로 분리
     *
     * @param source    원본 PDF
     * @param outputDir 출력 디렉토리
     * @param prefix    파일명 접두사
     */
    public static void splitAllPages(Path source, Path outputDir, String prefix) throws IOException {
        Files.createDirectories(outputDir);

        PdfReader reader = new PdfReader(source.toString());
        int totalPages = reader.getNumberOfPages();

        for (int i = 1; i <= totalPages; i++) {
            Path outputPath = outputDir.resolve(prefix + "_" + i + ".pdf");
            Document document = new Document();

            try (OutputStream os = Files.newOutputStream(outputPath)) {
                PdfCopy copy = new PdfCopy(document, os);
                document.open();
                copy.addPage(copy.getImportedPage(reader, i));
            } catch (Exception e) {
                throw new IOException("PDF 분리 실패: 페이지 " + i, e);
            } finally {
                document.close();
            }
        }

        reader.close();
    }

    /**
     * PDF 페이지 수 조회
     */
    public static int getPageCount(Path pdfPath) throws IOException {
        try (PdfReader reader = new PdfReader(pdfPath.toString())) {
            return reader.getNumberOfPages();
        }
    }
}
