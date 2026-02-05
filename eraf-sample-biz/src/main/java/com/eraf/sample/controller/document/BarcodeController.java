package com.eraf.sample.controller.document;

import com.eraf.barcode.BarcodeGenerator;
import com.eraf.barcode.QRCodeGenerator;
import com.eraf.core.utils.Base64Utils;
import com.google.zxing.BarcodeFormat;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Barcode/QR 기능 시연 컨트롤러
 * #8 Barcode 생성
 * #9 QR Code 생성
 */
@Slf4j
@Controller
@RequestMapping("/document/barcode")
public class BarcodeController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("menu", "barcode");
        model.addAttribute("title", "Barcode/QR - ERAF Sample");
        return "document/barcode";
    }

    /**
     * #8 Barcode 생성 - Code128 (GET)
     */
    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateBarcode(
            @RequestParam(defaultValue = "ERAF-123456") String content,
            @RequestParam(defaultValue = "CODE_128") String format,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "100") int height) throws IOException {

        return doGenerateBarcode(content, format, width, height);
    }

    /**
     * #8 Barcode 생성 - Code128 (POST for frontend)
     */
    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateBarcodePost(@RequestBody Map<String, Object> request) throws IOException {
        String content = (String) request.getOrDefault("content", "ERAF-123456");
        String format = (String) request.getOrDefault("format", "CODE_128");
        int width = request.get("width") != null ? ((Number) request.get("width")).intValue() : 300;
        int height = request.get("height") != null ? ((Number) request.get("height")).intValue() : 100;

        return doGenerateBarcode(content, format, width, height);
    }

    private byte[] doGenerateBarcode(String content, String format, int width, int height) throws IOException {
        BarcodeFormat barcodeFormat = switch (format.toUpperCase()) {
            case "CODE_39" -> BarcodeFormat.CODE_39;
            case "EAN_13" -> BarcodeFormat.EAN_13;
            case "EAN_8" -> BarcodeFormat.EAN_8;
            case "UPC_A" -> BarcodeFormat.UPC_A;
            case "ITF" -> BarcodeFormat.ITF;
            default -> BarcodeFormat.CODE_128;
        };

        log.info("Barcode generated: {} ({})", content, format);
        return BarcodeGenerator.generateToBytes(content, barcodeFormat, width, height, "PNG");
    }

    /**
     * Barcode 다운로드
     */
    @GetMapping("/download")
    public void downloadBarcode(
            @RequestParam(defaultValue = "ERAF-123456") String content,
            @RequestParam(defaultValue = "CODE_128") String format,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "100") int height,
            HttpServletResponse response) throws IOException {

        BarcodeFormat barcodeFormat = BarcodeFormat.valueOf(format);
        byte[] imageBytes = BarcodeGenerator.generateToBytes(content, barcodeFormat, width, height, "PNG");

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"barcode_" + content + ".png\"");
        response.getOutputStream().write(imageBytes);
    }

    /**
     * #9 QR Code 생성 (GET)
     */
    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateQR(
            @RequestParam(defaultValue = "https://eraf.example.com") String content,
            @RequestParam(defaultValue = "300") int size) throws IOException {

        log.info("QR Code generated: {}", content);
        return QRCodeGenerator.generateToBytes(content, size);
    }

    /**
     * #9 QR Code 생성 (POST for frontend) - /qrcode endpoint
     */
    @PostMapping(value = "/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateQRPost(@RequestBody Map<String, Object> request) throws IOException {
        String content = (String) request.getOrDefault("content", "https://eraf.example.com");
        int size = request.get("size") != null ? ((Number) request.get("size")).intValue() : 300;

        log.info("QR Code generated: {}", content);
        return QRCodeGenerator.generateToBytes(content, size);
    }

    /**
     * QR Code 다운로드
     */
    @GetMapping("/qr/download")
    public void downloadQR(
            @RequestParam(defaultValue = "https://eraf.example.com") String content,
            @RequestParam(defaultValue = "300") int size,
            HttpServletResponse response) throws IOException {

        byte[] imageBytes = QRCodeGenerator.generateToBytes(content, size);

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"qrcode.png\"");
        response.getOutputStream().write(imageBytes);
    }

    /**
     * Barcode/QR JSON API (Base64 반환)
     */
    @PostMapping("/api/generate")
    @ResponseBody
    public Map<String, Object> generateApi(@RequestBody Map<String, Object> request) throws IOException {
        String type = (String) request.getOrDefault("type", "qr");
        String content = (String) request.getOrDefault("content", "ERAF");
        int size = (int) request.getOrDefault("size", 300);

        byte[] imageBytes;
        if ("barcode".equals(type)) {
            String format = (String) request.getOrDefault("format", "CODE_128");
            int width = (int) request.getOrDefault("width", 300);
            int height = (int) request.getOrDefault("height", 100);
            imageBytes = BarcodeGenerator.generateToBytes(content, BarcodeFormat.valueOf(format), width, height, "PNG");
        } else {
            imageBytes = QRCodeGenerator.generateToBytes(content, size);
        }

        String base64 = Base64Utils.encode(imageBytes);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("type", type);
        result.put("content", content);
        result.put("image", "data:image/png;base64," + base64);

        return result;
    }

    /**
     * vCard QR 생성
     */
    @GetMapping(value = "/qr/vcard", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateVCardQR(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam(defaultValue = "300") int size) throws IOException {

        String vcard = String.format("""
                BEGIN:VCARD
                VERSION:3.0
                N:%s
                TEL:%s
                EMAIL:%s
                END:VCARD
                """, name, phone, email);

        log.info("vCard QR generated for: {}", name);
        return QRCodeGenerator.generateToBytes(vcard, size);
    }

    /**
     * WiFi QR 생성
     */
    @GetMapping(value = "/qr/wifi", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] generateWifiQR(
            @RequestParam String ssid,
            @RequestParam String password,
            @RequestParam(defaultValue = "WPA") String encryption,
            @RequestParam(defaultValue = "300") int size) throws IOException {

        String wifi = String.format("WIFI:T:%s;S:%s;P:%s;;", encryption, ssid, password);

        log.info("WiFi QR generated for SSID: {}", ssid);
        return QRCodeGenerator.generateToBytes(wifi, size);
    }
}
