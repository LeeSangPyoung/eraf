package com.eraf.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 리플렉션 유틸리티
 * 클래스, 필드, 메서드, 어노테이션 동적 처리
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    // ===== 클래스 정보 =====

    /**
     * 클래스명으로 Class 객체 로드
     */
    public static Class<?> forName(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 클래스의 심플 이름 반환
     */
    public static String getSimpleName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return clazz.getSimpleName();
    }

    /**
     * 클래스의 패키지명 반환
     */
    public static String getPackageName(Class<?> clazz) {
        if (clazz == null || clazz.getPackage() == null) {
            return null;
        }
        return clazz.getPackage().getName();
    }

    /**
     * 클래스가 인터페이스인지 확인
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz != null && clazz.isInterface();
    }

    /**
     * 클래스가 추상 클래스인지 확인
     */
    public static boolean isAbstract(Class<?> clazz) {
        return clazz != null && Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 클래스가 Enum인지 확인
     */
    public static boolean isEnum(Class<?> clazz) {
        return clazz != null && clazz.isEnum();
    }

    /**
     * 클래스가 배열인지 확인
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz != null && clazz.isArray();
    }

    /**
     * 클래스가 기본형(Primitive)인지 확인
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz != null && clazz.isPrimitive();
    }

    // ===== 인스턴스 생성 =====

    /**
     * 기본 생성자로 인스턴스 생성
     */
    public static <T> T newInstance(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * 기본 생성자로 인스턴스 생성 (예외 무시)
     */
    public static <T> T newInstanceQuietly(Class<T> clazz) {
        try {
            return newInstance(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 생성자 파라미터로 인스턴스 생성
     */
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        if (clazz == null) {
            return null;
        }
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    // ===== 필드 처리 =====

    /**
     * 필드 값 가져오기
     */
    public static Object getFieldValue(Object target, String fieldName) {
        if (target == null || fieldName == null) {
            return null;
        }
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    /**
     * 필드 값 가져오기 (예외 무시)
     */
    public static Object getFieldValueQuietly(Object target, String fieldName) {
        try {
            return getFieldValue(target, fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 필드 값 설정하기
     */
    public static void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null || fieldName == null) {
            return;
        }
        try {
            Field field = findField(target.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + fieldName, e);
        }
    }

    /**
     * 필드 값 설정하기 (예외 무시)
     */
    public static void setFieldValueQuietly(Object target, String fieldName, Object value) {
        try {
            setFieldValue(target, fieldName, value);
        } catch (Exception e) {
            // 무시
        }
    }

    /**
     * 클래스에서 필드 찾기 (부모 클래스까지 탐색)
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 모든 필드 가져오기 (부모 클래스 포함)
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        if (clazz == null) {
            return fields;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 특정 타입의 필드만 가져오기
     */
    public static List<Field> getFieldsByType(Class<?> clazz, Class<?> fieldType) {
        return getAllFields(clazz).stream()
                .filter(field -> field.getType().equals(fieldType))
                .collect(Collectors.toList());
    }

    /**
     * 특정 어노테이션이 있는 필드만 가져오기
     */
    public static List<Field> getFieldsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getAllFields(clazz).stream()
                .filter(field -> field.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    // ===== 메서드 처리 =====

    /**
     * 메서드 호출
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) {
        if (target == null || methodName == null) {
            return null;
        }
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = findMethod(target.getClass(), methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    /**
     * 메서드 호출 (예외 무시)
     */
    public static Object invokeMethodQuietly(Object target, String methodName, Object... args) {
        try {
            return invokeMethod(target, methodName, args);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * static 메서드 호출
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return invokeMethod(null, clazz, methodName, args);
    }

    /**
     * 메서드 호출 (클래스 지정)
     */
    private static Object invokeMethod(Object target, Class<?> clazz, String methodName, Object... args) {
        if (clazz == null || methodName == null) {
            return null;
        }
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = findMethod(clazz, methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    /**
     * 메서드 찾기 (부모 클래스까지 탐색)
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        if (clazz == null || methodName == null) {
            return null;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 모든 메서드 가져오기 (부모 클래스 포함)
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        if (clazz == null) {
            return methods;
        }
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }
        return methods;
    }

    /**
     * 특정 어노테이션이 있는 메서드만 가져오기
     */
    public static List<Method> getMethodsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return getAllMethods(clazz).stream()
                .filter(method -> method.isAnnotationPresent(annotationType))
                .collect(Collectors.toList());
    }

    /**
     * 메서드명으로 메서드 찾기 (파라미터 무관)
     */
    public static List<Method> getMethodsByName(Class<?> clazz, String methodName) {
        return getAllMethods(clazz).stream()
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());
    }

    // ===== Getter/Setter =====

    /**
     * Getter 메서드인지 확인
     */
    public static boolean isGetter(Method method) {
        if (method == null) {
            return false;
        }
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is")) &&
               method.getParameterCount() == 0 &&
               !void.class.equals(method.getReturnType());
    }

    /**
     * Setter 메서드인지 확인
     */
    public static boolean isSetter(Method method) {
        if (method == null) {
            return false;
        }
        String name = method.getName();
        return name.startsWith("set") &&
               method.getParameterCount() == 1;
    }

    /**
     * 모든 Getter 메서드 가져오기
     */
    public static List<Method> getAllGetters(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(ReflectionUtils::isGetter)
                .collect(Collectors.toList());
    }

    /**
     * 모든 Setter 메서드 가져오기
     */
    public static List<Method> getAllSetters(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(ReflectionUtils::isSetter)
                .collect(Collectors.toList());
    }

    /**
     * 필드명으로 Getter 메서드 찾기
     */
    public static Method findGetter(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        String getterName = "get" + capitalize(fieldName);
        Method getter = findMethod(clazz, getterName);
        if (getter == null) {
            // boolean 타입은 is로 시작할 수 있음
            getterName = "is" + capitalize(fieldName);
            getter = findMethod(clazz, getterName);
        }
        return getter;
    }

    /**
     * 필드명으로 Setter 메서드 찾기
     */
    public static Method findSetter(Class<?> clazz, String fieldName, Class<?> paramType) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        String setterName = "set" + capitalize(fieldName);
        return findMethod(clazz, setterName, paramType);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // ===== 어노테이션 =====

    /**
     * 클래스에 어노테이션이 있는지 확인
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType) {
        return clazz != null && clazz.isAnnotationPresent(annotationType);
    }

    /**
     * 클래스의 어노테이션 가져오기
     */
    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationType) {
        if (clazz == null || annotationType == null) {
            return null;
        }
        return clazz.getAnnotation(annotationType);
    }

    /**
     * 필드의 어노테이션 가져오기
     */
    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationType) {
        if (field == null || annotationType == null) {
            return null;
        }
        return field.getAnnotation(annotationType);
    }

    /**
     * 메서드의 어노테이션 가져오기
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        if (method == null || annotationType == null) {
            return null;
        }
        return method.getAnnotation(annotationType);
    }

    /**
     * 클래스의 모든 어노테이션 가져오기
     */
    public static List<Annotation> getAllAnnotations(Class<?> clazz) {
        if (clazz == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(clazz.getAnnotations());
    }

    // ===== 상속 관계 =====

    /**
     * 클래스가 특정 클래스를 상속하거나 구현하는지 확인
     */
    public static boolean isAssignableFrom(Class<?> superType, Class<?> subType) {
        if (superType == null || subType == null) {
            return false;
        }
        return superType.isAssignableFrom(subType);
    }

    /**
     * 모든 슈퍼클래스 가져오기
     */
    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        List<Class<?>> superclasses = new ArrayList<>();
        if (clazz == null) {
            return superclasses;
        }
        Class<?> current = clazz.getSuperclass();
        while (current != null && current != Object.class) {
            superclasses.add(current);
            current = current.getSuperclass();
        }
        return superclasses;
    }

    /**
     * 모든 인터페이스 가져오기 (구현한 모든 인터페이스)
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        if (clazz == null) {
            return new ArrayList<>(interfaces);
        }
        Class<?> current = clazz;
        while (current != null) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        return new ArrayList<>(interfaces);
    }

    // ===== 객체 복사 =====

    /**
     * 객체의 필드 값을 다른 객체로 복사 (같은 이름의 필드만)
     */
    public static void copyFields(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        List<Field> sourceFields = getAllFields(source.getClass());
        for (Field sourceField : sourceFields) {
            Field targetField = findField(target.getClass(), sourceField.getName());
            if (targetField != null && targetField.getType().equals(sourceField.getType())) {
                try {
                    sourceField.setAccessible(true);
                    targetField.setAccessible(true);
                    Object value = sourceField.get(source);
                    targetField.set(target, value);
                } catch (Exception e) {
                    // 무시
                }
            }
        }
    }

    /**
     * 객체의 특정 필드만 복사
     */
    public static void copyFields(Object source, Object target, String... fieldNames) {
        if (source == null || target == null || fieldNames == null) {
            return;
        }
        for (String fieldName : fieldNames) {
            Object value = getFieldValueQuietly(source, fieldName);
            setFieldValueQuietly(target, fieldName, value);
        }
    }

    // ===== 제네릭 타입 =====

    /**
     * 제네릭 슈퍼클래스의 실제 타입 파라미터 가져오기
     */
    public static Class<?> getGenericSuperclassType(Class<?> clazz, int index) {
        if (clazz == null) {
            return null;
        }
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return null;
        }
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        if (index < 0 || index >= actualTypeArguments.length) {
            return null;
        }
        Type type = actualTypeArguments[index];
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        return null;
    }

    // ===== 유틸리티 =====

    /**
     * 클래스 정보를 문자열로 출력 (디버깅용)
     */
    public static String getClassInfo(Class<?> clazz) {
        if (clazz == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(clazz.getName()).append("\n");
        sb.append("Package: ").append(getPackageName(clazz)).append("\n");
        sb.append("Modifiers: ").append(Modifier.toString(clazz.getModifiers())).append("\n");
        sb.append("Fields: ").append(getAllFields(clazz).size()).append("\n");
        sb.append("Methods: ").append(getAllMethods(clazz).size()).append("\n");
        sb.append("Interfaces: ").append(getAllInterfaces(clazz).size()).append("\n");
        return sb.toString();
    }

    /**
     * 객체의 모든 필드와 값을 문자열로 출력 (디버깅용)
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getSimpleName()).append(" {\n");
        List<Field> fields = getAllFields(obj.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                sb.append("  ").append(field.getName()).append(" = ").append(value).append("\n");
            } catch (Exception e) {
                sb.append("  ").append(field.getName()).append(" = <error>\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
