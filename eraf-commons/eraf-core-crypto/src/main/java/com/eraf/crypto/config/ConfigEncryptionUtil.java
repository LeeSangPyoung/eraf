package com.eraf.crypto.config;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * 설정 암호화 유틸리티
 * application.yml의 민감 설정값을 암호화/복호화
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 암호화
 * String encrypted = ConfigEncryptionUtil.encrypt("myPassword", "mySecretValue");
 * // 결과: ENC(base64EncodedValue)
 *
 * // 복호화
 * String decrypted = ConfigEncryptionUtil.decrypt("myPassword", "ENC(base64EncodedValue)");
 * // 결과: mySecretValue
 * </pre>
 */
public class ConfigEncryptionUtil {

    private static final String DEFAULT_ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";
    private static final int DEFAULT_KEY_OBTENTION_ITERATIONS = 1000;
    private static final int DEFAULT_POOL_SIZE = 1;
    private static final String DEFAULT_SALT_GENERATOR = "org.jasypt.salt.RandomSaltGenerator";
    private static final String DEFAULT_IV_GENERATOR = "org.jasypt.iv.RandomIvGenerator";
    private static final String DEFAULT_STRING_OUTPUT_TYPE = "base64";
    private static final String DEFAULT_PREFIX = "ENC(";
    private static final String DEFAULT_SUFFIX = ")";

    /**
     * 문자열 암호화 (기본 설정)
     *
     * @param password 암호화 키
     * @param value 암호화할 값
     * @return ENC(암호화된값)
     */
    public static String encrypt(String password, String value) {
        return encrypt(password, value, DEFAULT_PREFIX, DEFAULT_SUFFIX);
    }

    /**
     * 문자열 암호화 (커스텀 Prefix/Suffix)
     *
     * @param password 암호화 키
     * @param value 암호화할 값
     * @param prefix Prefix (예: "ENC(")
     * @param suffix Suffix (예: ")")
     * @return prefix + 암호화된값 + suffix
     */
    public static String encrypt(String password, String value, String prefix, String suffix) {
        PooledPBEStringEncryptor encryptor = createEncryptor(password);
        String encrypted = encryptor.encrypt(value);
        return prefix + encrypted + suffix;
    }

    /**
     * 문자열 복호화 (기본 설정)
     *
     * @param password 암호화 키
     * @param encryptedValue ENC(암호화된값) 형식
     * @return 복호화된 원본 값
     */
    public static String decrypt(String password, String encryptedValue) {
        return decrypt(password, encryptedValue, DEFAULT_PREFIX, DEFAULT_SUFFIX);
    }

    /**
     * 문자열 복호화 (커스텀 Prefix/Suffix)
     *
     * @param password 암호화 키
     * @param encryptedValue prefix + 암호화된값 + suffix 형식
     * @param prefix Prefix (예: "ENC(")
     * @param suffix Suffix (예: ")")
     * @return 복호화된 원본 값
     */
    public static String decrypt(String password, String encryptedValue, String prefix, String suffix) {
        // Prefix/Suffix 제거
        String extractedValue = encryptedValue;
        if (extractedValue.startsWith(prefix)) {
            extractedValue = extractedValue.substring(prefix.length());
        }
        if (extractedValue.endsWith(suffix)) {
            extractedValue = extractedValue.substring(0, extractedValue.length() - suffix.length());
        }

        PooledPBEStringEncryptor encryptor = createEncryptor(password);
        return encryptor.decrypt(extractedValue);
    }

    /**
     * 암호화된 값인지 확인
     *
     * @param value 확인할 값
     * @return 암호화된 값이면 true
     */
    public static boolean isEncrypted(String value) {
        return isEncrypted(value, DEFAULT_PREFIX, DEFAULT_SUFFIX);
    }

    /**
     * 암호화된 값인지 확인 (커스텀 Prefix/Suffix)
     *
     * @param value 확인할 값
     * @param prefix Prefix
     * @param suffix Suffix
     * @return 암호화된 값이면 true
     */
    public static boolean isEncrypted(String value, String prefix, String suffix) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.startsWith(prefix) && value.endsWith(suffix);
    }

    /**
     * Encryptor 생성 (내부용)
     */
    private static PooledPBEStringEncryptor createEncryptor(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Encryption password cannot be null or empty");
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword(password);
        config.setAlgorithm(DEFAULT_ALGORITHM);
        config.setKeyObtentionIterations(DEFAULT_KEY_OBTENTION_ITERATIONS);
        config.setPoolSize(DEFAULT_POOL_SIZE);
        config.setSaltGeneratorClassName(DEFAULT_SALT_GENERATOR);
        config.setIvGeneratorClassName(DEFAULT_IV_GENERATOR);
        config.setStringOutputType(DEFAULT_STRING_OUTPUT_TYPE);

        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * CLI 실행 (암호화/복호화 테스트용)
     *
     * <p>사용법:</p>
     * <pre>
     * java -cp ... com.eraf.core.crypto.ConfigEncryptionUtil encrypt myPassword mySecretValue
     * java -cp ... com.eraf.core.crypto.ConfigEncryptionUtil decrypt myPassword ENC(...)
     * </pre>
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage:");
            System.out.println("  Encrypt: java ConfigEncryptionUtil encrypt <password> <value>");
            System.out.println("  Decrypt: java ConfigEncryptionUtil decrypt <password> <encryptedValue>");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  java ConfigEncryptionUtil encrypt myPassword \"jdbc:postgresql://localhost:5432/mydb\"");
            System.out.println("  java ConfigEncryptionUtil decrypt myPassword \"ENC(abcd1234...)\"");
            System.exit(1);
        }

        String mode = args[0];
        String password = args[1];
        String value = args[2];

        try {
            if ("encrypt".equalsIgnoreCase(mode)) {
                String encrypted = encrypt(password, value);
                System.out.println("Original: " + value);
                System.out.println("Encrypted: " + encrypted);
                System.out.println();
                System.out.println("Add to application.yml:");
                System.out.println("  property: " + encrypted);
            } else if ("decrypt".equalsIgnoreCase(mode)) {
                String decrypted = decrypt(password, value);
                System.out.println("Encrypted: " + value);
                System.out.println("Decrypted: " + decrypted);
            } else {
                System.err.println("Invalid mode: " + mode + " (use 'encrypt' or 'decrypt')");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
