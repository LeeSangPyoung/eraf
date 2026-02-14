# ERAF Barcode

ZXing 기반 바코드/QR코드 생성 및 읽기를 지원하는 모듈입니다.

## 기능

- **QR코드 생성**: 텍스트, URL, vCard 등
- **바코드 생성**: EAN-13, CODE-128, CODE-39 등
- **바코드 읽기**: 이미지에서 바코드/QR코드 인식
- **에러 보정**: L, M, Q, H 레벨 지원
- **다양한 포맷**: PNG, JPG, SVG

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-barcode</artifactId>
</dependency>
```

## 사용법

### 1. QR코드 생성

```java
import com.eraf.barcode.QRCodeGenerator;
import java.nio.file.Path;

public void generateQRCode() throws IOException {
    String content = "https://example.com";
    Path outputPath = Path.of("qrcode.png");
    int size = 300;  // 300x300 pixels

    QRCodeGenerator.generate(content, outputPath, size);
}
```

### 2. 에러 보정 레벨 지정

```java
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public void generateWithErrorCorrection() throws IOException {
    QRCodeGenerator.generate(
        "Important Data",
        Path.of("qrcode.png"),
        300,
        ErrorCorrectionLevel.H  // 30% 복원 가능
    );
}
```

**에러 보정 레벨**:
- `L`: 7% 복원 가능
- `M`: 15% 복원 가능 (기본값)
- `Q`: 25% 복원 가능
- `H`: 30% 복원 가능

### 3. QR코드 바이트 배열 생성

```java
public byte[] generateQRCodeBytes(String content) throws IOException {
    return QRCodeGenerator.generateBytes(content, 300);
}

// HTTP Response로 반환
@GetMapping("/qrcode")
public ResponseEntity<byte[]> getQRCode(@RequestParam String content) throws IOException {
    byte[] qrCode = QRCodeGenerator.generateBytes(content, 300);
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .body(qrCode);
}
```

### 4. 바코드 생성

```java
import com.eraf.barcode.BarcodeGenerator;
import com.google.zxing.BarcodeFormat;

public void generateBarcode() throws IOException {
    // EAN-13 바코드
    BarcodeGenerator.generate(
        "9788956746425",
        BarcodeFormat.EAN_13,
        Path.of("ean13.png"),
        300,
        100
    );

    // CODE-128 바코드
    BarcodeGenerator.generate(
        "CODE128-12345",
        BarcodeFormat.CODE_128,
        Path.of("code128.png"),
        300,
        100
    );
}
```

### 5. 바코드/QR코드 읽기

```java
import com.eraf.barcode.BarcodeReader;

public String readBarcode(Path imagePath) throws IOException {
    return BarcodeReader.read(imagePath);
}

public void scanAndProcess() throws IOException {
    Path qrCodeImage = Path.of("qrcode.png");
    String content = BarcodeReader.read(qrCodeImage);

    System.out.println("QR Code content: " + content);

    if (content.startsWith("http")) {
        System.out.println("URL detected: " + content);
    }
}
```

## 고급 기능

### 1. vCard QR코드

```java
public void generateVCardQRCode() throws IOException {
    String vCard = """
        BEGIN:VCARD
        VERSION:3.0
        N:홍;길동
        FN:홍길동
        TEL;TYPE=CELL:010-1234-5678
        EMAIL:hong@example.com
        URL:https://example.com
        END:VCARD
        """;

    QRCodeGenerator.generate(vCard, Path.of("contact.png"), 300);
}
```

### 2. WiFi QR코드

```java
public void generateWiFiQRCode() throws IOException {
    String ssid = "MyWiFi";
    String password = "password123";
    String encryption = "WPA";  // WPA, WEP, nopass

    String wifiConfig = String.format(
        "WIFI:T:%s;S:%s;P:%s;;",
        encryption, ssid, password
    );

    QRCodeGenerator.generate(wifiConfig, Path.of("wifi.png"), 300);
}
```

### 3. SMS QR코드

```java
public void generateSmsQRCode() throws IOException {
    String phoneNumber = "01012345678";
    String message = "Hello from QR Code";

    String smsContent = String.format("SMSTO:%s:%s", phoneNumber, message);

    QRCodeGenerator.generate(smsContent, Path.of("sms.png"), 300);
}
```

### 4. 이메일 QR코드

```java
public void generateEmailQRCode() throws IOException {
    String to = "recipient@example.com";
    String subject = "Subject Here";
    String body = "Email body content";

    String mailto = String.format(
        "MAILTO:%s?subject=%s&body=%s",
        to,
        URLEncoder.encode(subject, StandardCharsets.UTF_8),
        URLEncoder.encode(body, StandardCharsets.UTF_8)
    );

    QRCodeGenerator.generate(mailto, Path.of("email.png"), 300);
}
```

### 5. 로고 삽입 QR코드

```java
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public void generateQRCodeWithLogo(String content, Path logoPath, Path outputPath) throws IOException {
    // 1. QR코드 생성
    byte[] qrBytes = QRCodeGenerator.generateBytes(content, 300);
    BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrBytes));

    // 2. 로고 이미지 로드 및 리사이즈
    BufferedImage logo = ImageIO.read(logoPath.toFile());
    int logoSize = qrImage.getWidth() / 5;
    Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);

    // 3. QR코드 중앙에 로고 삽입
    Graphics2D g = qrImage.createGraphics();
    int x = (qrImage.getWidth() - logoSize) / 2;
    int y = (qrImage.getHeight() - logoSize) / 2;
    g.drawImage(scaledLogo, x, y, null);
    g.dispose();

    // 4. 저장
    ImageIO.write(qrImage, "PNG", outputPath.toFile());
}
```

### 6. 색상 지정 QR코드

```java
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageConfig;

public void generateColoredQRCode(String content, Path outputPath) throws IOException {
    int onColor = 0xFF0000FF;  // Blue
    int offColor = 0xFFFFFFFF; // White

    MatrixToImageConfig config = new MatrixToImageConfig(onColor, offColor);

    BitMatrix bitMatrix = new MultiFormatWriter().encode(
        content,
        BarcodeFormat.QR_CODE,
        300,
        300
    );

    MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath, config);
}
```

### 7. 배치 생성

```java
public void generateBatchQRCodes(List<String> contents, Path outputDir) throws IOException {
    for (int i = 0; i < contents.size(); i++) {
        Path outputPath = outputDir.resolve("qrcode_" + i + ".png");
        QRCodeGenerator.generate(contents.get(i), outputPath, 300);
    }
}
```

## 바코드 형식

### 지원되는 형식

```java
// 1차원 바코드
BarcodeFormat.EAN_8
BarcodeFormat.EAN_13
BarcodeFormat.UPC_A
BarcodeFormat.UPC_E
BarcodeFormat.CODE_39
BarcodeFormat.CODE_93
BarcodeFormat.CODE_128
BarcodeFormat.ITF

// 2차원 바코드
BarcodeFormat.QR_CODE
BarcodeFormat.DATA_MATRIX
BarcodeFormat.AZTEC
BarcodeFormat.PDF_417
```

### 바코드 검증

```java
public boolean isValidEAN13(String code) {
    if (code.length() != 13) {
        return false;
    }

    try {
        BarcodeGenerator.generate(code, BarcodeFormat.EAN_13, Path.of("temp.png"), 300, 100);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

## 실전 예제

### 제품 라벨 QR코드

```java
@Service
public class ProductLabelService {

    public byte[] generateProductQRCode(Product product) throws IOException {
        String json = objectMapper.writeValueAsString(Map.of(
            "id", product.getId(),
            "name", product.getName(),
            "price", product.getPrice(),
            "url", "https://example.com/products/" + product.getId()
        ));

        return QRCodeGenerator.generateBytes(json, 300, ErrorCorrectionLevel.H);
    }
}
```

### 티켓 바코드

```java
@Service
public class TicketService {

    public byte[] generateTicketBarcode(Ticket ticket) throws IOException {
        String barcodeContent = String.format(
            "TICKET-%s-%s",
            ticket.getEventId(),
            ticket.getSeatNumber()
        );

        return BarcodeGenerator.generateBytes(
            barcodeContent,
            BarcodeFormat.CODE_128,
            400,
            100
        );
    }

    public Ticket validateTicket(Path barcodeImage) throws IOException {
        String content = BarcodeReader.read(barcodeImage);
        String[] parts = content.split("-");

        if (parts.length != 3 || !parts[0].equals("TICKET")) {
            throw new BusinessException("Invalid ticket barcode");
        }

        return ticketRepository.findByEventIdAndSeatNumber(parts[1], parts[2])
            .orElseThrow(() -> new BusinessException("Ticket not found"));
    }
}
```

### 재고 관리 QR코드

```java
@Service
public class InventoryService {

    public void generateInventoryQRCodes(Path outputDir) throws IOException {
        List<InventoryItem> items = inventoryRepository.findAll();

        for (InventoryItem item : items) {
            String content = String.format(
                "SKU:%s|Name:%s|Qty:%d|Location:%s",
                item.getSku(),
                item.getName(),
                item.getQuantity(),
                item.getLocation()
            );

            Path qrPath = outputDir.resolve(item.getSku() + ".png");
            QRCodeGenerator.generate(content, qrPath, 200);
        }
    }

    public InventoryItem scanInventory(Path qrCodeImage) throws IOException {
        String content = BarcodeReader.read(qrCodeImage);
        Map<String, String> data = parseInventoryQRCode(content);
        return inventoryRepository.findBySku(data.get("SKU"));
    }
}
```

## 모범 사례

1. **QR코드 크기**: 최소 200x200, 권장 300x300 이상
2. **에러 보정**: 손상 가능성 높으면 H 레벨 사용
3. **데이터 크기**: QR코드는 최대 4,296자 (숫자 기준)
4. **테스트**: 생성 후 반드시 읽기 테스트
5. **색상 대비**: 충분한 대비로 인식률 향상
6. **여백**: QR코드 주변 충분한 여백 확보
7. **포맷 선택**: URL/텍스트는 QR, 숫자는 바코드

## 참고

- [ZXing](https://github.com/zxing/zxing)
- [QR Code Specification](https://www.qrcode.com/en/about/standards.html)
- [Barcode Standards](https://www.gs1.org/standards/barcodes)
