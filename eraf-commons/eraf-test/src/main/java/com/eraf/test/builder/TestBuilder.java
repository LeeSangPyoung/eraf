package com.eraf.test.builder;

import java.util.function.Consumer;

/**
 * Generic Test Object Builder
 *
 * <p>사용 예시:</p>
 * <pre>
 * User user = TestBuilder.of(User::new)
 *     .with(User::setName, "John Doe")
 *     .with(User::setEmail, "john@example.com")
 *     .with(User::setAge, 30)
 *     .build();
 *
 * // List 생성
 * List&lt;User&gt; users = TestBuilder.of(User::new)
 *     .with(User::setName, TestDataGenerator.fullName())
 *     .with(User::setEmail, TestDataGenerator.email())
 *     .buildList(10);
 * </pre>
 *
 * @param <T> Type of object to build
 */
public class TestBuilder<T> {

    private final java.util.function.Supplier<T> supplier;
    private final java.util.List<Consumer<T>> modifiers = new java.util.ArrayList<>();

    private TestBuilder(java.util.function.Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Create builder from supplier
     */
    public static <T> TestBuilder<T> of(java.util.function.Supplier<T> supplier) {
        return new TestBuilder<>(supplier);
    }

    /**
     * Create builder from existing instance (single-use, buildList will return same instance)
     */
    public static <T> TestBuilder<T> from(T instance) {
        return new TestBuilder<>(() -> instance);
    }

    /**
     * Set property using setter method reference
     *
     * @param setter Setter method reference (e.g., User::setName)
     * @param value  Value to set
     */
    public <U> TestBuilder<T> with(java.util.function.BiConsumer<T, U> setter, U value) {
        modifiers.add(obj -> setter.accept(obj, value));
        return this;
    }

    /**
     * Apply custom modification
     *
     * @param modifier Consumer that modifies the instance
     */
    public TestBuilder<T> apply(Consumer<T> modifier) {
        modifiers.add(modifier);
        return this;
    }

    /**
     * Build the object
     */
    public T build() {
        T instance = supplier.get();
        modifiers.forEach(m -> m.accept(instance));
        return instance;
    }

    /**
     * Build list of objects with same configuration.
     * Each call to the supplier creates a new instance.
     *
     * @param count Number of objects to create
     */
    public java.util.List<T> buildList(int count) {
        java.util.List<T> list = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(build());
        }
        return list;
    }
}
