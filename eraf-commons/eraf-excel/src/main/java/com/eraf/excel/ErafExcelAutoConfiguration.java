package com.eraf.excel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Excel Auto-Configuration
 *
 * <p>Excel 파일 읽기/쓰기 기능을 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>Excel 파일 읽기 (XLS, XLSX)</li>
 *   <li>Excel 파일 쓰기</li>
 *   <li>스트리밍 방식 대용량 Excel 쓰기</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Excel 읽기
 * {@code @Autowired}
 * private ExcelReader excelReader;
 *
 * List&lt;Map&lt;String, Object&gt;&gt; data = excelReader.read(inputStream);
 *
 * // Excel 쓰기
 * {@code @Autowired}
 * private ExcelWriter excelWriter;
 *
 * excelWriter.write(outputStream, data, headers);
 *
 * // 대용량 스트리밍 쓰기
 * {@code @Autowired}
 * private StreamingExcelWriter streamingWriter;
 *
 * streamingWriter.write(outputStream, dataIterator);
 * </pre>
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.apache.poi.ss.usermodel.Workbook")
public class ErafExcelAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafExcelAutoConfiguration.class);

    /**
     * Excel 라이터 빈 등록
     *
     * Note: ExcelReader는 팩토리 메서드(ExcelReader.open())로 생성하므로 Bean으로 등록하지 않음
     */
    @Bean
    @ConditionalOnMissingBean
    public ExcelWriter excelWriter() {
        log.info("Registering ExcelWriter - Excel writing support enabled");
        return new ExcelWriter();
    }

    /**
     * 스트리밍 Excel 라이터 빈 등록 (대용량 처리)
     */
    @Bean
    @ConditionalOnMissingBean
    public StreamingExcelWriter streamingExcelWriter() {
        log.info("Registering StreamingExcelWriter - Large Excel file streaming support enabled");
        return new StreamingExcelWriter();
    }
}
