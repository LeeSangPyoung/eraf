package com.eraf.core.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PDF 생성 유틸리티
 */
public final class PdfGenerator {

    private PdfGenerator() {
    }

    /**
     * HTML을 PDF로 변환
     */
    public static void fromHtml(String html, Path outputPath) throws IOException {
        fromHtml(html, outputPath, null);
    }

    /**
     * HTML을 PDF로 변환 (폰트 경로 지정)
     */
    public static void fromHtml(String html, Path outputPath, String fontPath) throws IOException {
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            ITextRenderer renderer = new ITextRenderer();

            if (fontPath != null) {
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            }

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        } catch (DocumentException e) {
            throw new IOException("PDF 생성 실패", e);
        }
    }

    /**
     * HTML을 PDF 바이트 배열로 변환
     */
    public static byte[] fromHtmlToBytes(String html, String fontPath) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            if (fontPath != null) {
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            }

            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(baos);
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("PDF 생성 실패", e);
        }
    }

    /**
     * 간단한 텍스트 PDF 생성
     */
    public static void createTextPdf(String text, Path outputPath) throws IOException {
        Document document = new Document();
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfWriter.getInstance(document, os);
            document.open();
            document.add(new Paragraph(text));
        } catch (DocumentException e) {
            throw new IOException("PDF 생성 실패", e);
        } finally {
            document.close();
        }
    }

    /**
     * 빈 PDF 생성
     */
    public static void createEmpty(Path outputPath, int pages) throws IOException {
        Document document = new Document();
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            PdfWriter.getInstance(document, os);
            document.open();
            for (int i = 0; i < pages; i++) {
                document.newPage();
                document.add(new Paragraph(" "));
            }
        } catch (DocumentException e) {
            throw new IOException("PDF 생성 실패", e);
        } finally {
            document.close();
        }
    }
}
