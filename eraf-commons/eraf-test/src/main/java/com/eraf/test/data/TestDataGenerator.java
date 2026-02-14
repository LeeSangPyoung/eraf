package com.eraf.test.data;

import net.datafaker.Faker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Test Data Generator using DataFaker
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 기본 사용
 * String name = TestDataGenerator.fullName();
 * String email = TestDataGenerator.email();
 * int age = TestDataGenerator.randomInt(18, 65);
 *
 * // List 생성
 * List&lt;User&gt; users = TestDataGenerator.list(10, () -&gt; new User(
 *     TestDataGenerator.fullName(),
 *     TestDataGenerator.email()
 * ));
 *
 * // 한글 데이터
 * TestDataGenerator korean = TestDataGenerator.korean();
 * String koreanName = korean.fullName();
 * </pre>
 */
public class TestDataGenerator {

    private final Faker faker;
    private final Random random;

    private static final TestDataGenerator DEFAULT_INSTANCE = new TestDataGenerator(Locale.ENGLISH);
    private static final TestDataGenerator KOREAN_INSTANCE = new TestDataGenerator(Locale.KOREA);

    public TestDataGenerator() {
        this(Locale.ENGLISH);
    }

    public TestDataGenerator(Locale locale) {
        this.faker = new Faker(locale);
        this.random = new Random();
    }

    /**
     * Get default generator (English)
     */
    public static TestDataGenerator instance() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Get Korean generator
     */
    public static TestDataGenerator korean() {
        return KOREAN_INSTANCE;
    }

    // ==================== Static Convenience Methods ====================

    public static String fullName() {
        return DEFAULT_INSTANCE.faker.name().fullName();
    }

    public static String firstName() {
        return DEFAULT_INSTANCE.faker.name().firstName();
    }

    public static String lastName() {
        return DEFAULT_INSTANCE.faker.name().lastName();
    }

    public static String email() {
        return DEFAULT_INSTANCE.faker.internet().emailAddress();
    }

    public static String username() {
        return DEFAULT_INSTANCE.faker.internet().username();
    }

    public static String phoneNumber() {
        return DEFAULT_INSTANCE.faker.phoneNumber().phoneNumber();
    }

    public static String address() {
        return DEFAULT_INSTANCE.faker.address().fullAddress();
    }

    public static String city() {
        return DEFAULT_INSTANCE.faker.address().city();
    }

    public static String country() {
        return DEFAULT_INSTANCE.faker.address().country();
    }

    public static String zipCode() {
        return DEFAULT_INSTANCE.faker.address().zipCode();
    }

    public static String companyName() {
        return DEFAULT_INSTANCE.faker.company().name();
    }

    public static String jobTitle() {
        return DEFAULT_INSTANCE.faker.job().title();
    }

    public static String sentence() {
        return DEFAULT_INSTANCE.faker.lorem().sentence();
    }

    public static String paragraph() {
        return DEFAULT_INSTANCE.faker.lorem().paragraph();
    }

    public static String word() {
        return DEFAULT_INSTANCE.faker.lorem().word();
    }

    public static String text(int maxLength) {
        String text = DEFAULT_INSTANCE.faker.lorem().characters(maxLength);
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    public static String uuid() {
        return DEFAULT_INSTANCE.faker.internet().uuid();
    }

    public static String url() {
        return DEFAULT_INSTANCE.faker.internet().url();
    }

    public static String ipAddress() {
        return DEFAULT_INSTANCE.faker.internet().ipV4Address();
    }

    // ==================== Numbers ====================

    public static int randomInt() {
        return DEFAULT_INSTANCE.random.nextInt();
    }

    public static int randomInt(int max) {
        return DEFAULT_INSTANCE.random.nextInt(max);
    }

    public static int randomInt(int min, int max) {
        return DEFAULT_INSTANCE.random.nextInt(max - min + 1) + min;
    }

    public static long randomLong() {
        return DEFAULT_INSTANCE.random.nextLong();
    }

    public static long randomLong(long max) {
        return Math.abs(DEFAULT_INSTANCE.random.nextLong()) % max;
    }

    public static long randomLong(long min, long max) {
        return Math.abs(DEFAULT_INSTANCE.random.nextLong()) % (max - min + 1) + min;
    }

    public static double randomDouble() {
        return DEFAULT_INSTANCE.random.nextDouble();
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * DEFAULT_INSTANCE.random.nextDouble();
    }

    public static boolean randomBoolean() {
        return DEFAULT_INSTANCE.random.nextBoolean();
    }

    // ==================== Dates ====================

    public static LocalDate pastDate() {
        return DEFAULT_INSTANCE.faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static LocalDate futureDate() {
        return DEFAULT_INSTANCE.faker.date().future(365, java.util.concurrent.TimeUnit.DAYS)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static LocalDateTime pastDateTime() {
        return DEFAULT_INSTANCE.faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime futureDateTime() {
        return DEFAULT_INSTANCE.faker.date().future(365, java.util.concurrent.TimeUnit.DAYS)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Instant pastInstant() {
        return DEFAULT_INSTANCE.faker.date().past(365, java.util.concurrent.TimeUnit.DAYS).toInstant();
    }

    public static Instant futureInstant() {
        return DEFAULT_INSTANCE.faker.date().future(365, java.util.concurrent.TimeUnit.DAYS).toInstant();
    }

    // ==================== Collections ====================

    /**
     * Generate list of test data
     *
     * @param count    Number of items
     * @param supplier Supplier function
     * @return List of generated items
     */
    public static <T> List<T> list(int count, Supplier<T> supplier) {
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    /**
     * Pick random element from array
     */
    @SafeVarargs
    public static <T> T oneOf(T... options) {
        return options[DEFAULT_INSTANCE.random.nextInt(options.length)];
    }

    /**
     * Pick random element from list
     */
    public static <T> T oneOf(List<T> options) {
        return options.get(DEFAULT_INSTANCE.random.nextInt(options.size()));
    }

    // ==================== Instance Methods ====================

    public Faker getFaker() {
        return faker;
    }

    public Random getRandom() {
        return random;
    }

    // Instance versions of static methods for custom locales
    public String instanceFullName() {
        return faker.name().fullName();
    }

    public String instanceEmail() {
        return faker.internet().emailAddress();
    }

    public String instancePhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }

    public String instanceAddress() {
        return faker.address().fullAddress();
    }

    public String instanceCompanyName() {
        return faker.company().name();
    }
}
