package com.eraf.sample.controller.document;

import com.eraf.core.file.FileUtils;
import com.eraf.core.utils.Base64Utils;
import com.eraf.image.ImageUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Image 기능 시연 컨트롤러
 * #10 이미지 리사이즈
 * #11 썸네일 생성
 * #12 이미지 워터마크
 */
@Slf4j
@Controller
@RequestMapping("/document/image")
public class ImageController {

    @Value("${app.upload.path:${java.io.tmpdir}}")
    private String uploadPath;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("menu", "image");
        model.addAttribute("title", "Image - ERAF Sample");
        return "document/image";
    }

    /**
     * #10 이미지 리사이즈
     */
    @PostMapping("/resize")
    @ResponseBody
    public Map<String, Object> resize(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "400") int width,
            @RequestParam(defaultValue = "300") int height,
            @RequestParam(defaultValue = "true") boolean keepRatio) throws IOException {

        Path sourcePath = saveUploadedFile(file);
        Path targetPath = Files.createTempFile("resized_", ".png");

        try {
            if (keepRatio) {
                ImageUtils.resize(sourcePath, targetPath, width, height);
            } else {
                ImageUtils.resizeForce(sourcePath, targetPath, width, height);
            }

            ImageUtils.ImageInfo originalInfo = ImageUtils.getInfo(sourcePath);
            ImageUtils.ImageInfo resizedInfo = ImageUtils.getInfo(targetPath);

            String base64 = Base64Utils.encode(FileUtils.readBytes(targetPath));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("original", Map.of("width", originalInfo.width(), "height", originalInfo.height()));
            result.put("resized", Map.of("width", resizedInfo.width(), "height", resizedInfo.height()));
            result.put("image", "data:image/png;base64," + base64);

            log.info("Image resized: {}x{} -> {}x{}", originalInfo.width(), originalInfo.height(), resizedInfo.width(), resizedInfo.height());
            return result;
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(targetPath);
        }
    }

    /**
     * #11 썸네일 생성
     */
    @PostMapping("/thumbnail")
    @ResponseBody
    public Map<String, Object> thumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "150") int size) throws IOException {

        Path sourcePath = saveUploadedFile(file);
        Path targetPath = Files.createTempFile("thumb_", ".png");

        try {
            ImageUtils.thumbnail(sourcePath, targetPath, size);

            ImageUtils.ImageInfo originalInfo = ImageUtils.getInfo(sourcePath);
            ImageUtils.ImageInfo thumbInfo = ImageUtils.getInfo(targetPath);

            String base64 = Base64Utils.encode(FileUtils.readBytes(targetPath));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("original", Map.of("width", originalInfo.width(), "height", originalInfo.height()));
            result.put("thumbnail", Map.of("width", thumbInfo.width(), "height", thumbInfo.height()));
            result.put("image", "data:image/png;base64," + base64);

            log.info("Thumbnail created: {}x{} -> {}x{}", originalInfo.width(), originalInfo.height(), thumbInfo.width(), thumbInfo.height());
            return result;
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(targetPath);
        }
    }

    /**
     * #12 이미지 워터마크
     */
    @PostMapping("/watermark")
    @ResponseBody
    public Map<String, Object> watermark(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "ERAF Sample") String text,
            @RequestParam(defaultValue = "0.5") float opacity) throws IOException {

        Path sourcePath = saveUploadedFile(file);
        Path targetPath = Files.createTempFile("watermark_", ".png");

        try {
            ImageUtils.watermark(sourcePath, targetPath, text, opacity);

            String base64 = Base64Utils.encode(FileUtils.readBytes(targetPath));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("watermarkText", text);
            result.put("opacity", opacity);
            result.put("image", "data:image/png;base64," + base64);

            log.info("Watermark added: {} (opacity: {})", text, opacity);
            return result;
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(targetPath);
        }
    }

    /**
     * 이미지 회전
     */
    @PostMapping("/rotate")
    @ResponseBody
    public Map<String, Object> rotate(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "90") double degrees) throws IOException {

        Path sourcePath = saveUploadedFile(file);
        Path targetPath = Files.createTempFile("rotated_", ".png");

        try {
            ImageUtils.rotate(sourcePath, targetPath, degrees);

            String base64 = Base64Utils.encode(FileUtils.readBytes(targetPath));

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("degrees", degrees);
            result.put("image", "data:image/png;base64," + base64);

            log.info("Image rotated: {} degrees", degrees);
            return result;
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(targetPath);
        }
    }

    /**
     * 이미지 정보 조회
     */
    @PostMapping("/info")
    @ResponseBody
    public Map<String, Object> info(@RequestParam("file") MultipartFile file) throws IOException {
        Path sourcePath = saveUploadedFile(file);

        try {
            ImageUtils.ImageInfo info = ImageUtils.getInfo(sourcePath);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("filename", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("width", info.width());
            result.put("height", info.height());
            result.put("format", info.format());

            return result;
        } finally {
            Files.deleteIfExists(sourcePath);
        }
    }

    /**
     * 포맷 변환
     */
    @PostMapping("/convert")
    @ResponseBody
    public Map<String, Object> convert(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "png") String format) throws IOException {

        Path sourcePath = saveUploadedFile(file);
        Path targetPath = Files.createTempFile("converted_", "." + format);

        try {
            ImageUtils.convert(sourcePath, targetPath, format);

            String base64 = Base64Utils.encode(FileUtils.readBytes(targetPath));
            String mimeType = switch (format.toLowerCase()) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "gif" -> "image/gif";
                case "bmp" -> "image/bmp";
                default -> "image/png";
            };

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("format", format);
            result.put("image", "data:" + mimeType + ";base64," + base64);

            log.info("Image converted to: {}", format);
            return result;
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(targetPath);
        }
    }

    /**
     * 다운로드
     */
    @PostMapping("/download")
    public void download(
            @RequestParam("file") MultipartFile file,
            @RequestParam String operation,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Float opacity,
            @RequestParam(required = false) Double degrees,
            HttpServletResponse response) throws IOException {

        Path sourcePath = saveUploadedFile(file);
        Path targetPath = Files.createTempFile("output_", ".png");

        try {
            switch (operation) {
                case "resize" -> ImageUtils.resize(sourcePath, targetPath, width != null ? width : 400, height != null ? height : 300);
                case "thumbnail" -> ImageUtils.thumbnail(sourcePath, targetPath, size != null ? size : 150);
                case "watermark" -> ImageUtils.watermark(sourcePath, targetPath, text != null ? text : "ERAF", opacity != null ? opacity : 0.5f);
                case "rotate" -> ImageUtils.rotate(sourcePath, targetPath, degrees != null ? degrees : 90);
            }

            byte[] imageBytes = Files.readAllBytes(targetPath);

            response.setContentType("image/png");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + operation + "_image.png\"");
            response.getOutputStream().write(imageBytes);
        } finally {
            Files.deleteIfExists(sourcePath);
            Files.deleteIfExists(targetPath);
        }
    }

    private Path saveUploadedFile(MultipartFile file) throws IOException {
        String extension = FileUtils.getExtension(file.getOriginalFilename());
        if (extension == null || extension.isEmpty()) {
            extension = "png";
        }
        Path path = Files.createTempFile("upload_" + UUID.randomUUID(), "." + extension);
        file.transferTo(path.toFile());
        return path;
    }
}
