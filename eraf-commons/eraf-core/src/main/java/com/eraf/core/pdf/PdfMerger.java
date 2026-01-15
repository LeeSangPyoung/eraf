package com.eraf.core.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * PDF 병합 유틸리티
 */
public final class PdfMerger {

    private PdfMerger() {
    }

    /**
     * 여러 PDF 파일 병합
     */
    public static void merge(List<Path> pdfFiles, Path outputPath) throws IOException {
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            throw new IllegalArgumentException("병합할 PDF 파일이 없습니다");
        }

        Document document = new Document();
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfCopy copy = new PdfCopy(document, os);
            document.open();

            for (Path pdfFile : pdfFiles) {
                PdfReader reader = new PdfReader(pdfFile.toString());
                int pages = reader.getNumberOfPages();

                for (int i = 1; i <= pages; i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }

                reader.close();
            }
        } catch (Exception e) {
            throw new IOException("PDF 병합 실패", e);
        } finally {
            document.close();
        }
    }

    /**
     * 두 PDF 파일 병합
     */
    public static void merge(Path pdf1, Path pdf2, Path outputPath) throws IOException {
        merge(List.of(pdf1, pdf2), outputPath);
    }
}
