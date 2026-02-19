package com.eraf.barcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Barcode Auto-Configuration
 *
 * <p>바코드 및 QR 코드 생성/읽기 기능을 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>바코드 생성 및 읽기 (EAN-13, Code-128, etc.)</li>
 *   <li>QR 코드 생성 및 읽기</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // QR 코드 생성 (static methods)
 * byte[] qrImage = QRCodeGenerator.generateToBytes("https://example.com", 300);
 *
 * // 바코드 생성 (static methods)
 * byte[] barcode = BarcodeGenerator.generateEAN13("1234567890128");
 *
 * // 바코드 읽기 (static methods)
 * String decoded = BarcodeReader.read(imageBytes);
 * </pre>
 *
 * <p>Note: 모든 클래스가 static utility methods를 제공하므로 별도의 Bean 등록이 불필요합니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.google.zxing.BarcodeFormat")
public class ErafBarcodeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafBarcodeAutoConfiguration.class);

    public ErafBarcodeAutoConfiguration() {
        log.info("ERAF Barcode module loaded - QRCodeGenerator, BarcodeGenerator, BarcodeReader available as static utilities");
    }
}
