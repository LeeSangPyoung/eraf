package com.eraf.notification.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * NotificationTemplateEngine 테스트
 */
class NotificationTemplateEngineTest {

    private NotificationTemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new NotificationTemplateEngine();
    }

    // ===== 기본 변수 치환 =====

    @Test
    @DisplayName("단순 변수 치환")
    void testSimpleVariable() {
        String template = "안녕하세요 {{userName}}님";
        Map<String, Object> vars = Map.of("userName", "홍길동");

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("안녕하세요 홍길동님");
    }

    @Test
    @DisplayName("다중 변수 치환")
    void testMultipleVariables() {
        String template = "{{userName}}님, 주문번호 {{orderNumber}}이 {{status}} 되었습니다.";
        Map<String, Object> vars = Map.of(
                "userName", "홍길동",
                "orderNumber", "ORD-2024-001",
                "status", "확인"
        );

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("홍길동님, 주문번호 ORD-2024-001이 확인 되었습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 변수 - 빈 문자열로 대체")
    void testMissingVariable() {
        String template = "Hello {{name}}, your code is {{code}}";
        Map<String, Object> vars = Map.of("name", "John");

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("Hello John, your code is ");
    }

    @Test
    @DisplayName("중첩 변수 (dot notation)")
    void testNestedVariable() {
        String template = "주소: {{address.city}} {{address.street}}";
        Map<String, Object> vars = Map.of(
                "address", Map.of("city", "서울시", "street", "강남대로 123")
        );

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("주소: 서울시 강남대로 123");
    }

    @Test
    @DisplayName("변수명 전후 공백 무시")
    void testVariableWithSpaces() {
        String template = "Hello {{ name }}!";
        Map<String, Object> vars = Map.of("name", "World");

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("Hello World!");
    }

    // ===== 조건부 블록 =====

    @Test
    @DisplayName("조건부 블록 - true")
    void testIfBlockTrue() {
        String template = "Hello{{#if premium}} Premium{{/if}} Member";
        Map<String, Object> vars = Map.of("premium", true);

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("Hello Premium Member");
    }

    @Test
    @DisplayName("조건부 블록 - false")
    void testIfBlockFalse() {
        String template = "Hello{{#if premium}} Premium{{/if}} Member";
        Map<String, Object> vars = Map.of("premium", false);

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("Hello Member");
    }

    @Test
    @DisplayName("조건부 블록 - null (falsy)")
    void testIfBlockNull() {
        String template = "Hello{{#if premium}} Premium{{/if}} Member";
        Map<String, Object> vars = new HashMap<>();

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("Hello Member");
    }

    @Test
    @DisplayName("조건부 블록 - 비어있지 않은 문자열 (truthy)")
    void testIfBlockNonEmptyString() {
        String template = "{{#if couponCode}}쿠폰코드: {{couponCode}}{{/if}}";
        Map<String, Object> vars = Map.of("couponCode", "SAVE10");

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("쿠폰코드: SAVE10");
    }

    // ===== 반복 블록 =====

    @Test
    @DisplayName("반복 블록 - 리스트 항목")
    void testEachBlock() {
        String template = "상품 목록:{{#each items}}\n- {{name}} ({{price}}원){{/each}}";
        Map<String, Object> vars = Map.of(
                "items", List.of(
                        Map.of("name", "노트북", "price", "1500000"),
                        Map.of("name", "마우스", "price", "50000")
                )
        );

        String result = engine.render(template, vars);

        assertThat(result).contains("- 노트북 (1500000원)");
        assertThat(result).contains("- 마우스 (50000원)");
    }

    @Test
    @DisplayName("반복 블록 - 빈 리스트")
    void testEachBlockEmpty() {
        String template = "상품:{{#each items}}{{name}}{{/each}}끝";
        Map<String, Object> vars = Map.of("items", List.of());

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("상품:끝");
    }

    // ===== 기본값 =====

    @Test
    @DisplayName("기본값 - 변수 없을 때")
    void testDefaultValueMissing() {
        String template = "안녕하세요 {{name|default:\"고객\"}}님";
        Map<String, Object> vars = new HashMap<>();

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("안녕하세요 고객님");
    }

    @Test
    @DisplayName("기본값 - 변수 있을 때")
    void testDefaultValuePresent() {
        String template = "안녕하세요 {{name|default:\"고객\"}}님";
        Map<String, Object> vars = Map.of("name", "홍길동");

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("안녕하세요 홍길동님");
    }

    // ===== 변수 추출 =====

    @Test
    @DisplayName("변수 추출")
    void testExtractVariables() {
        String template = "{{userName}}님, 주문 {{orderNumber}} 상태: {{status}}";

        Set<String> vars = engine.extractVariables(template);

        assertThat(vars).containsExactly("userName", "orderNumber", "status");
    }

    @Test
    @DisplayName("변수 추출 - 중복 제거")
    void testExtractVariablesDedup() {
        String template = "{{name}} {{name}} {{email}}";

        Set<String> vars = engine.extractVariables(template);

        assertThat(vars).hasSize(2);
        assertThat(vars).containsExactly("name", "email");
    }

    // ===== 필수 변수 검증 =====

    @Test
    @DisplayName("필수 변수 검증 - 모두 충족")
    void testValidateVariablesAllPresent() {
        NotificationTemplate template = new NotificationTemplate();
        template.setRequiredVariables("userName,orderNumber");

        Map<String, Object> vars = Map.of("userName", "홍길동", "orderNumber", "ORD-001");

        List<String> missing = engine.validateVariables(template, vars);

        assertThat(missing).isEmpty();
    }

    @Test
    @DisplayName("필수 변수 검증 - 누락")
    void testValidateVariablesMissing() {
        NotificationTemplate template = new NotificationTemplate();
        template.setRequiredVariables("userName,orderNumber,email");

        Map<String, Object> vars = Map.of("userName", "홍길동");

        List<String> missing = engine.validateVariables(template, vars);

        assertThat(missing).containsExactlyInAnyOrder("orderNumber", "email");
    }

    // ===== 엣지 케이스 =====

    @Test
    @DisplayName("NULL 템플릿")
    void testNullTemplate() {
        assertThat(engine.render(null, Map.of())).isNull();
    }

    @Test
    @DisplayName("빈 템플릿")
    void testEmptyTemplate() {
        assertThat(engine.render("", Map.of())).isEmpty();
    }

    @Test
    @DisplayName("NULL 변수")
    void testNullVariables() {
        String result = engine.render("Hello {{name}}", null);
        assertThat(result).isEqualTo("Hello ");
    }

    @Test
    @DisplayName("변수 없는 템플릿")
    void testTemplateWithoutVariables() {
        String result = engine.render("Hello World", Map.of());
        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("특수문자 포함 변수값")
    void testSpecialCharacters() {
        String template = "메시지: {{message}}";
        Map<String, Object> vars = Map.of("message", "Hello $100 (50% off)");

        String result = engine.render(template, vars);

        assertThat(result).isEqualTo("메시지: Hello $100 (50% off)");
    }

    // ===== 복합 템플릿 =====

    @Test
    @DisplayName("복합 템플릿 - 이메일 주문 확인")
    void testComplexEmailTemplate() {
        String template = """
                안녕하세요 {{userName}}님,

                주문번호 {{orderNumber}}이 확인되었습니다.

                {{#if premium}}[프리미엄 회원] 무료배송이 적용됩니다.{{/if}}

                주문 상품:{{#each items}}
                - {{name}}: {{price}}원{{/each}}

                감사합니다.""";

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "홍길동");
        vars.put("orderNumber", "ORD-2024-001");
        vars.put("premium", true);
        vars.put("items", List.of(
                Map.of("name", "노트북", "price", "1500000"),
                Map.of("name", "마우스", "price", "50000")
        ));

        String result = engine.render(template, vars);

        assertThat(result).contains("홍길동님");
        assertThat(result).contains("ORD-2024-001");
        assertThat(result).contains("[프리미엄 회원] 무료배송이 적용됩니다.");
        assertThat(result).contains("- 노트북: 1500000원");
        assertThat(result).contains("- 마우스: 50000원");
    }
}
