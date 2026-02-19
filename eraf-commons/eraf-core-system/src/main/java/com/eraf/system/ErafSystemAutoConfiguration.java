package com.eraf.system;

import com.eraf.code.CodeRepository;
import com.eraf.code.CodeService;
import com.eraf.code.InMemoryCodeRepository;
import com.eraf.sequence.SequenceAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * ERAF System Auto-Configuration
 *
 * <p>공통코드(Code) 관리와 시퀀스(Sequence) 생성 기능을 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>공통코드 관리 서비스 (CodeService)</li>
 *   <li>자동 시퀀스 번호 생성 (SequenceAspect with @Sequence annotation)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // CodeService 사용
 * {@code @Autowired}
 * private CodeService codeService;
 *
 * List&lt;CodeItem&gt; codes = codeService.getByGroup("USER_TYPE");
 *
 * // @Sequence 어노테이션 사용
 * public class Order {
 *     {@code @Sequence}(name = "ORDER_NO", prefix = "ORD", digits = 6, reset = Reset.DAILY)
 *     private String orderNo;
 * }
 * </pre>
 */
@AutoConfiguration
@EnableAspectJAutoProxy
public class ErafSystemAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafSystemAutoConfiguration.class);

    /**
     * In-Memory Code Repository 빈 등록 (기본 구현)
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeRepository codeRepository() {
        log.debug("Registering InMemoryCodeRepository (default implementation)");
        return new InMemoryCodeRepository();
    }

    /**
     * Code Service 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeService codeService(CodeRepository codeRepository) {
        log.info("Registering CodeService for common code management");
        return new CodeService(codeRepository);
    }

    /**
     * Sequence Aspect 빈 등록 (Aspect가 classpath에 있을 때만)
     */
    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnMissingBean
    public SequenceAspect sequenceAspect() {
        log.info("Registering SequenceAspect - @Sequence annotation support enabled");
        return new SequenceAspect();
    }
}
