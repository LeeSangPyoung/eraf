package com.eraf.core.sequence;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Field;

/**
 * 채번 AOP Aspect
 * @Sequence 어노테이션이 붙은 필드에 자동으로 시퀀스 값을 설정
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SequenceAspect {

    private static final Logger log = LoggerFactory.getLogger(SequenceAspect.class);

    private final SequenceProvider sequenceProvider;

    public SequenceAspect() {
        this.sequenceProvider = new InMemorySequenceProvider();
    }

    public SequenceAspect(SequenceProvider sequenceProvider) {
        this.sequenceProvider = sequenceProvider;
    }

    /**
     * Repository save 메서드 인터셉트
     */
    @Before("execution(* org.springframework.data.repository.CrudRepository+.save(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository+.save(..))")
    public void beforeSave(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] != null) {
            populateSequenceFields(args[0]);
        }
    }

    /**
     * @GenerateSequence 어노테이션이 붙은 메서드 인터셉트
     */
    @Before("@annotation(generateSequence)")
    public void beforeGenerateSequence(JoinPoint joinPoint, GenerateSequence generateSequence) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                populateSequenceFields(arg);
            }
        }
    }

    /**
     * 객체의 @Sequence 필드에 시퀀스 값 설정
     */
    public void populateSequenceFields(Object target) {
        if (target == null) {
            return;
        }

        Class<?> clazz = target.getClass();
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(Sequence.class)) {
                try {
                    field.setAccessible(true);
                    Object currentValue = field.get(target);

                    // 이미 값이 있으면 건너뜀
                    if (currentValue != null && !currentValue.toString().isEmpty()) {
                        continue;
                    }

                    Sequence seq = field.getAnnotation(Sequence.class);
                    String sequenceName = seq.name().isEmpty() ? field.getName() : seq.name();

                    String generatedValue = sequenceProvider.next(
                            sequenceName,
                            seq.prefix(),
                            seq.reset(),
                            seq.digits(),
                            seq.dateSeparator(),
                            seq.dateFormat()
                    );

                    field.set(target, generatedValue);
                    log.debug("Generated sequence for field '{}': {}", field.getName(), generatedValue);

                } catch (IllegalAccessException e) {
                    log.error("Failed to set sequence field: {}", field.getName(), e);
                }
            }
        }
    }

    /**
     * 클래스와 부모 클래스의 모든 필드 조회
     */
    private Field[] getAllFields(Class<?> clazz) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(java.util.Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * 시퀀스 제공자 인터페이스
     */
    public interface SequenceProvider {
        String next(String name, String prefix, Reset reset, int digits,
                   String dateSeparator, String dateFormat);
    }

    /**
     * 인메모리 시퀀스 제공자 (기본 구현)
     */
    public static class InMemorySequenceProvider implements SequenceProvider {
        @Override
        public String next(String name, String prefix, Reset reset, int digits,
                          String dateSeparator, String dateFormat) {
            return SequenceGenerator.next(name, prefix, reset, digits, dateSeparator, dateFormat);
        }
    }
}
