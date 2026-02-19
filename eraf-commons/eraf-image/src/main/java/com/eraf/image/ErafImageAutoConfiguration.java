package com.eraf.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * ERAF Image Auto-Configuration
 *
 * <p>이미지 처리 기능을 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>이미지 리사이즈 (비율 유지/강제)</li>
 *   <li>이미지 크롭</li>
 *   <li>워터마크 추가</li>
 *   <li>이미지 회전</li>
 *   <li>형식 변환 (PNG, JPEG 등)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // ImageUtils는 static 메서드로 제공되므로 직접 사용
 * ImageUtils.resize(sourcePath, targetPath, 800, 600);
 * ImageUtils.watermark(sourcePath, targetPath, watermarkPath, Positions.BOTTOM_RIGHT);
 * ImageUtils.rotate(sourcePath, targetPath, 90);
 * </pre>
 *
 * <p>Note: ImageUtils는 static 유틸리티 클래스이므로 Bean 주입이 아닌 직접 호출로 사용합니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "net.coobird.thumbnailator.Thumbnails")
public class ErafImageAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafImageAutoConfiguration.class);

    public ErafImageAutoConfiguration() {
        log.info("ERAF Image module enabled - Image processing utilities available");
        log.debug("Use ImageUtils static methods for image resize, crop, watermark, rotation");
    }
}
