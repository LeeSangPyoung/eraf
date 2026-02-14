# ERAF Image

Thumbnailator 기반 이미지 처리를 지원하는 모듈입니다.

## 기능

- **리사이즈**: 비율 유지/강제 리사이즈, 스케일 조정
- **크롭**: 중앙/좌표 기반 크롭
- **회전**: 각도 지정 회전
- **워터마크**: 텍스트/이미지 워터마크
- **포맷 변환**: JPG, PNG, GIF, WebP 변환
- **압축**: 품질 조정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-image</artifactId>
</dependency>
```

## 사용법

### 1. 이미지 리사이즈

```java
import com.eraf.image.ImageUtils;
import java.nio.file.Path;

// 비율 유지 리사이즈
public void resizeImage() throws IOException {
    Path source = Path.of("original.jpg");
    Path target = Path.of("resized.jpg");

    ImageUtils.resize(source, target, 800, 600);  // 최대 800x600, 비율 유지
}

// 강제 리사이즈
public void forceResize() throws IOException {
    ImageUtils.resizeForce(source, target, 800, 600);  // 정확히 800x600
}

// 스케일 비율
public void scaleImage() throws IOException {
    ImageUtils.scale(source, target, 0.5);  // 50% 크기로 축소
}
```

### 2. 이미지 크롭

```java
// 중앙 크롭
public void cropCenter() throws IOException {
    ImageUtils.cropCenter(source, target, 400, 400);  // 중앙 400x400 추출
}

// 좌표 지정 크롭
public void cropByCoordinates() throws IOException {
    int x = 100, y = 100;
    int width = 300, height = 300;

    ImageUtils.crop(source, target, x, y, width, height);
}
```

### 3. 이미지 회전

```java
public void rotateImage() throws IOException {
    ImageUtils.rotate(source, target, 90);   // 90도 회전
    ImageUtils.rotate(source, target, 180);  // 180도 회전
    ImageUtils.rotate(source, target, -45);  // 반시계방향 45도
}
```

### 4. 워터마크

```java
// 텍스트 워터마크
public void addTextWatermark() throws IOException {
    ImageUtils.watermark(
        source,
        target,
        "Copyright © 2024",
        0.5f  // 투명도 50%
    );
}

// 이미지 워터마크
public void addImageWatermark() throws IOException {
    Path watermarkImage = Path.of("logo.png");
    ImageUtils.watermarkImage(source, target, watermarkImage, 0.7f);
}
```

### 5. 포맷 변환

```java
// JPG → PNG
public void convertToPng() throws IOException {
    ImageUtils.convert(Path.of("image.jpg"), Path.of("image.png"), "PNG");
}

// PNG → JPG
public void convertToJpg() throws IOException {
    ImageUtils.convert(Path.of("image.png"), Path.of("image.jpg"), "JPG");
}

// WebP 변환
public void convertToWebP() throws IOException {
    ImageUtils.convert(source, Path.of("image.webp"), "WEBP");
}
```

### 6. 이미지 압축

```java
// 품질 조정 (0.0 ~ 1.0)
public void compressImage() throws IOException {
    ImageUtils.compress(source, target, 0.8f);  // 80% 품질
}

// 리사이즈 + 압축
public void resizeAndCompress() throws IOException {
    ImageUtils.resizeWithQuality(source, target, 800, 600, 0.7f);
}
```

## 고급 기능

### 1. 썸네일 생성

```java
public void generateThumbnails() throws IOException {
    Path original = Path.of("photo.jpg");

    // 다양한 크기 썸네일 생성
    ImageUtils.resize(original, Path.of("thumb_small.jpg"), 150, 150);
    ImageUtils.resize(original, Path.of("thumb_medium.jpg"), 300, 300);
    ImageUtils.resize(original, Path.of("thumb_large.jpg"), 600, 600);
}
```

### 2. 배치 처리

```java
public void batchResize(Path inputDir, Path outputDir, int width, int height) throws IOException {
    Files.walk(inputDir)
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().matches(".*\\.(jpg|jpeg|png)$"))
        .forEach(source -> {
            try {
                Path target = outputDir.resolve(source.getFileName());
                ImageUtils.resize(source, target, width, height);
            } catch (IOException e) {
                log.error("Failed to resize: " + source, e);
            }
        });
}
```

### 3. 이미지 정보 추출

```java
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public Map<String, Object> getImageInfo(Path imagePath) throws IOException {
    BufferedImage image = ImageIO.read(imagePath.toFile());

    return Map.of(
        "width", image.getWidth(),
        "height", image.getHeight(),
        "type", image.getType(),
        "colorModel", image.getColorModel().toString(),
        "fileSize", Files.size(imagePath)
    );
}
```

### 4. 이미지 필터

```java
import net.coobird.thumbnailator.filters.*;

public void applyFilter() throws IOException {
    Thumbnails.of(source.toFile())
        .size(800, 600)
        .addFilter(new Brightness(1.2f))  // 밝기 증가
        .addFilter(new Contrast(1.1f))    // 대비 증가
        .toFile(target.toFile());
}

public void convertToGrayscale() throws IOException {
    BufferedImage original = ImageIO.read(source.toFile());
    BufferedImage grayscale = new BufferedImage(
        original.getWidth(),
        original.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY
    );

    Graphics2D g = grayscale.createGraphics();
    g.drawImage(original, 0, 0, null);
    g.dispose();

    ImageIO.write(grayscale, "jpg", target.toFile());
}
```

### 5. 프로필 이미지 처리

```java
@Service
public class ProfileImageService {

    public byte[] processProfileImage(MultipartFile file) throws IOException {
        // 1. 임시 파일 저장
        Path tempFile = Files.createTempFile("profile-", ".jpg");
        file.transferTo(tempFile);

        // 2. 정사각형 크롭 (중앙)
        Path croppedFile = Files.createTempFile("cropped-", ".jpg");
        ImageUtils.cropCenter(tempFile, croppedFile, 400, 400);

        // 3. 압축
        Path compressedFile = Files.createTempFile("compressed-", ".jpg");
        ImageUtils.compress(croppedFile, compressedFile, 0.8f);

        // 4. 바이트 배열로 변환
        byte[] result = Files.readAllBytes(compressedFile);

        // 5. 임시 파일 삭제
        Files.delete(tempFile);
        Files.delete(croppedFile);
        Files.delete(compressedFile);

        return result;
    }
}
```

### 6. 반응형 이미지 생성

```java
@Service
public class ResponsiveImageService {

    public void generateResponsiveImages(Path original, String baseName, Path outputDir) throws IOException {
        Map<String, Integer> sizes = Map.of(
            "xs", 320,
            "sm", 640,
            "md", 1024,
            "lg", 1920,
            "xl", 2560
        );

        sizes.forEach((suffix, width) -> {
            try {
                Path target = outputDir.resolve(baseName + "-" + suffix + ".jpg");
                ImageUtils.resize(original, target, width, Integer.MAX_VALUE);
                ImageUtils.compress(target, target, 0.85f);
            } catch (IOException e) {
                log.error("Failed to generate responsive image: " + suffix, e);
            }
        });
    }
}
```

### 7. 이미지 조합

```java
public void combineImages(List<Path> images, Path output) throws IOException {
    List<BufferedImage> bufferedImages = new ArrayList<>();
    int totalHeight = 0;
    int maxWidth = 0;

    for (Path imagePath : images) {
        BufferedImage img = ImageIO.read(imagePath.toFile());
        bufferedImages.add(img);
        totalHeight += img.getHeight();
        maxWidth = Math.max(maxWidth, img.getWidth());
    }

    BufferedImage combined = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = combined.createGraphics();

    int y = 0;
    for (BufferedImage img : bufferedImages) {
        g.drawImage(img, 0, y, null);
        y += img.getHeight();
    }
    g.dispose();

    ImageIO.write(combined, "jpg", output.toFile());
}
```

## HTTP Response로 이미지 반환

```java
@RestController
public class ImageController {

    @GetMapping("/images/thumbnail/{id}")
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Long id) throws IOException {
        Path originalImage = getImagePath(id);
        Path thumbnail = Files.createTempFile("thumb-", ".jpg");

        try {
            ImageUtils.resize(originalImage, thumbnail, 300, 300);
            ImageUtils.compress(thumbnail, thumbnail, 0.8f);

            byte[] imageBytes = Files.readAllBytes(thumbnail);

            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
                .body(imageBytes);

        } finally {
            Files.deleteIfExists(thumbnail);
        }
    }
}
```

## Base64 인코딩/디코딩

```java
public String encodeToBase64(Path imagePath) throws IOException {
    byte[] imageBytes = Files.readAllBytes(imagePath);
    return Base64.getEncoder().encodeToString(imageBytes);
}

public void decodeFromBase64(String base64String, Path outputPath) throws IOException {
    byte[] imageBytes = Base64.getDecoder().decode(base64String);
    Files.write(outputPath, imageBytes);
}

// Data URL 생성 (HTML img src 사용)
public String toDataUrl(Path imagePath) throws IOException {
    String base64 = encodeToBase64(imagePath);
    String extension = getExtension(imagePath);
    return String.format("data:image/%s;base64,%s", extension, base64);
}
```

## 실전 예제

### 상품 이미지 처리

```java
@Service
public class ProductImageService {

    public void processProductImages(MultipartFile originalFile, Long productId) throws IOException {
        Path tempOriginal = Files.createTempFile("product-", ".jpg");
        originalFile.transferTo(tempOriginal);

        Path outputDir = Path.of("products", productId.toString());
        Files.createDirectories(outputDir);

        // 원본 저장 (고품질)
        Path original = outputDir.resolve("original.jpg");
        ImageUtils.compress(tempOriginal, original, 0.95f);

        // 상세 페이지용 (중간 크기)
        Path detail = outputDir.resolve("detail.jpg");
        ImageUtils.resizeWithQuality(tempOriginal, detail, 1200, 1200, 0.9f);

        // 목록용 썸네일
        Path thumbnail = outputDir.resolve("thumbnail.jpg");
        ImageUtils.cropCenter(tempOriginal, thumbnail, 400, 400);
        ImageUtils.compress(thumbnail, thumbnail, 0.85f);

        // 모바일용
        Path mobile = outputDir.resolve("mobile.jpg");
        ImageUtils.resize(tempOriginal, mobile, 600, 600);

        Files.delete(tempOriginal);
    }
}
```

## 에러 처리

```java
public void processImageSafely(Path source, Path target) {
    try {
        if (!Files.exists(source)) {
            throw new BusinessException("Image file not found");
        }

        if (Files.size(source) > 10_000_000) {  // 10MB
            throw new BusinessException("Image file too large");
        }

        ImageUtils.resize(source, target, 800, 600);

    } catch (IOException e) {
        log.error("Image processing failed", e);
        throw new BusinessException("Failed to process image", e);
    }
}
```

## 모범 사례

1. **비율 유지**: 일반적으로 비율 유지 리사이즈 사용
2. **압축**: 웹용 이미지는 70-85% 품질로 압축
3. **포맷 선택**: 사진은 JPG, 투명 배경은 PNG
4. **썸네일**: 다양한 크기 사전 생성으로 성능 향상
5. **임시 파일**: try-finally로 반드시 삭제
6. **캐싱**: 처리된 이미지는 CDN 또는 캐시 활용
7. **검증**: 파일 크기, 형식 검증 필수

## 참고

- [Thumbnailator](https://github.com/coobird/thumbnailator)
- [Java ImageIO](https://docs.oracle.com/javase/tutorial/2d/images/)
- [Image Optimization Best Practices](https://web.dev/fast/#optimize-your-images)
