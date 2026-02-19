package com.eraf.http;

import com.eraf.util.converter.JsonConverter;
import okhttp3.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 클라이언트 (OkHttp 래핑)
 *
 * <p>내부적으로 공유 ConnectionPool을 사용하여 커넥션을 재활용합니다.
 * 애플리케이션 종료 시 {@link #close()}를 호출하여 리소스를 해제하세요.</p>
 */
public class ErafHttpClient implements Closeable {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * 모든 ErafHttpClient 인스턴스가 공유하는 ConnectionPool
     */
    private static final ConnectionPool SHARED_CONNECTION_POOL = new ConnectionPool(
            20, 5, TimeUnit.MINUTES);

    private String baseUrl;
    private Duration timeout = Duration.ofSeconds(30);
    private int retryCount = 0;
    private final Map<String, String> defaultHeaders = new HashMap<>();
    private final List<Interceptor> interceptors = new ArrayList<>();
    private volatile OkHttpClient client;

    private ErafHttpClient() {
    }

    /**
     * 새 클라이언트 인스턴스 생성
     */
    public static ErafHttpClient create() {
        return new ErafHttpClient();
    }

    /**
     * Base URL 설정
     */
    public ErafHttpClient baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * 타임아웃 설정
     */
    public ErafHttpClient timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 재시도 횟수 설정
     */
    public ErafHttpClient retry(int count) {
        this.retryCount = count;
        return this;
    }

    /**
     * 기본 헤더 추가
     */
    public ErafHttpClient header(String name, String value) {
        this.defaultHeaders.put(name, value);
        return this;
    }

    /**
     * Authorization 헤더 설정
     */
    public ErafHttpClient bearerToken(String token) {
        return header("Authorization", "Bearer " + token);
    }

    /**
     * OkHttp 인터셉터 추가
     *
     * @param interceptor OkHttp Interceptor
     */
    public ErafHttpClient addInterceptor(Interceptor interceptor) {
        this.interceptors.add(interceptor);
        this.client = null; // 인터셉터 변경 시 클라이언트 재생성
        return this;
    }

    /**
     * GET 요청
     */
    public <T> T get(String path, Class<T> responseType) throws IOException {
        Request request = buildRequest(path)
                .get()
                .build();
        return execute(request, responseType);
    }

    /**
     * GET 요청 (쿼리 파라미터)
     */
    public <T> T get(String path, Map<String, String> queryParams, Class<T> responseType) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(buildUrl(path)).newBuilder();
        queryParams.forEach(urlBuilder::addQueryParameter);

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .get();
        defaultHeaders.forEach(requestBuilder::header);

        return execute(requestBuilder.build(), responseType);
    }

    /**
     * POST 요청
     */
    public <T> T post(String path, Object body, Class<T> responseType) throws IOException {
        String json = body != null ? JsonConverter.toJson(body) : "";
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = buildRequest(path)
                .post(requestBody)
                .build();
        return execute(request, responseType);
    }

    /**
     * POST 요청 (응답 없음)
     */
    public void post(String path, Object body) throws IOException {
        post(path, body, Void.class);
    }

    /**
     * PUT 요청
     */
    public <T> T put(String path, Object body, Class<T> responseType) throws IOException {
        String json = body != null ? JsonConverter.toJson(body) : "";
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = buildRequest(path)
                .put(requestBody)
                .build();
        return execute(request, responseType);
    }

    /**
     * DELETE 요청
     */
    public <T> T delete(String path, Class<T> responseType) throws IOException {
        Request request = buildRequest(path)
                .delete()
                .build();
        return execute(request, responseType);
    }

    /**
     * DELETE 요청 (응답 없음)
     */
    public void delete(String path) throws IOException {
        delete(path, Void.class);
    }

    /**
     * PATCH 요청
     */
    public <T> T patch(String path, Object body, Class<T> responseType) throws IOException {
        String json = body != null ? JsonConverter.toJson(body) : "";
        RequestBody requestBody = RequestBody.create(json, JSON);

        Request request = buildRequest(path)
                .patch(requestBody)
                .build();
        return execute(request, responseType);
    }

    // ========== 파일 업로드/다운로드 ==========

    /**
     * 파일 업로드 (Multipart)
     *
     * @param path      API 경로
     * @param file      업로드할 파일
     * @param fieldName 폼 필드명
     * @return 응답 본문
     */
    public <T> T uploadFile(String path, File file, String fieldName, Class<T> responseType) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        RequestBody fileBody = RequestBody.create(file, MediaType.parse(contentType));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fieldName, file.getName(), fileBody)
                .build();

        Request request = buildRequest(path)
                .post(multipartBody)
                .build();
        return execute(request, responseType);
    }

    /**
     * 파일 업로드 (Multipart, 응답 없음)
     */
    public void uploadFile(String path, File file, String fieldName) throws IOException {
        uploadFile(path, file, fieldName, Void.class);
    }

    /**
     * 바이트 배열 업로드 (Multipart)
     *
     * @param path        API 경로
     * @param data        업로드할 데이터
     * @param fileName    파일명
     * @param fieldName   폼 필드명
     * @param contentType 콘텐츠 타입
     * @return 응답 본문
     */
    public <T> T uploadFile(String path, byte[] data, String fileName, String fieldName,
                           String contentType, Class<T> responseType) throws IOException {
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        RequestBody fileBody = RequestBody.create(data, MediaType.parse(contentType));
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fieldName, fileName, fileBody)
                .build();

        Request request = buildRequest(path)
                .post(multipartBody)
                .build();
        return execute(request, responseType);
    }

    /**
     * 바이트 배열 업로드 (Multipart, 응답 없음)
     */
    public void uploadFile(String path, byte[] data, String fileName, String fieldName) throws IOException {
        uploadFile(path, data, fileName, fieldName, null, Void.class);
    }

    /**
     * 여러 파일 업로드 (Multipart)
     *
     * @param path   API 경로
     * @param files  업로드할 파일 맵 (필드명 -> 파일)
     * @return 응답 본문
     */
    public <T> T uploadFiles(String path, Map<String, File> files, Class<T> responseType) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        for (Map.Entry<String, File> entry : files.entrySet()) {
            File file = entry.getValue();
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            RequestBody fileBody = RequestBody.create(file, MediaType.parse(contentType));
            builder.addFormDataPart(entry.getKey(), file.getName(), fileBody);
        }

        Request request = buildRequest(path)
                .post(builder.build())
                .build();
        return execute(request, responseType);
    }

    /**
     * 파일과 폼 데이터 함께 업로드 (Multipart)
     *
     * @param path      API 경로
     * @param file      업로드할 파일
     * @param fieldName 파일 필드명
     * @param formData  추가 폼 데이터
     * @return 응답 본문
     */
    public <T> T uploadFileWithData(String path, File file, String fieldName,
                                    Map<String, String> formData, Class<T> responseType) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fieldName, file.getName(),
                        RequestBody.create(file, MediaType.parse(contentType)));

        if (formData != null) {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }

        Request request = buildRequest(path)
                .post(builder.build())
                .build();
        return execute(request, responseType);
    }

    /**
     * 파일 다운로드 (바이트 배열로 반환)
     *
     * @param path API 경로
     * @return 파일 데이터
     */
    public byte[] downloadFile(String path) throws IOException {
        if (client == null) {
            client = buildClient();
        }

        Request request = buildRequest(path)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code(), "파일 다운로드 실패: " + response.code());
            }
            if (response.body() == null) {
                return new byte[0];
            }
            return response.body().bytes();
        }
    }

    /**
     * 파일 다운로드 (쿼리 파라미터 포함)
     *
     * @param path        API 경로
     * @param queryParams 쿼리 파라미터
     * @return 파일 데이터
     */
    public byte[] downloadFile(String path, Map<String, String> queryParams) throws IOException {
        if (client == null) {
            client = buildClient();
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(buildUrl(path)).newBuilder();
        queryParams.forEach(urlBuilder::addQueryParameter);

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .get();
        defaultHeaders.forEach(requestBuilder::header);

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code(), "파일 다운로드 실패: " + response.code());
            }
            if (response.body() == null) {
                return new byte[0];
            }
            return response.body().bytes();
        }
    }

    /**
     * 파일 다운로드 (파일로 저장)
     *
     * @param path        API 경로
     * @param destination 저장할 파일
     * @return 다운로드된 바이트 수
     */
    public long downloadFile(String path, File destination) throws IOException {
        if (client == null) {
            client = buildClient();
        }

        Request request = buildRequest(path)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code(), "파일 다운로드 실패: " + response.code());
            }
            if (response.body() == null) {
                return 0;
            }

            // 부모 디렉토리 생성
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (InputStream inputStream = response.body().byteStream();
                 FileOutputStream outputStream = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                long totalBytes = 0;
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                return totalBytes;
            }
        }
    }

    /**
     * 스트리밍 다운로드 (대용량 파일용)
     *
     * @param path     API 경로
     * @param callback 스트림 처리 콜백
     */
    public void downloadFileStreaming(String path, StreamCallback callback) throws IOException {
        if (client == null) {
            client = buildClient();
        }

        Request request = buildRequest(path)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new HttpException(response.code(), "파일 다운로드 실패: " + response.code());
            }
            if (response.body() != null) {
                String contentType = response.header("Content-Type");
                long contentLength = response.body().contentLength();
                callback.onStream(response.body().byteStream(), contentType, contentLength);
            }
        }
    }

    /**
     * 스트림 콜백 인터페이스
     */
    @FunctionalInterface
    public interface StreamCallback {
        void onStream(InputStream inputStream, String contentType, long contentLength) throws IOException;
    }

    private Request.Builder buildRequest(String path) {
        Request.Builder builder = new Request.Builder()
                .url(buildUrl(path));
        defaultHeaders.forEach(builder::header);
        return builder;
    }

    private String buildUrl(String path) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return path;
        }
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl + path.substring(1);
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private <T> T execute(Request request, Class<T> responseType) throws IOException {
        if (client == null) {
            client = buildClient();
        }

        int attempts = 0;
        IOException lastException = null;

        while (attempts <= retryCount) {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new HttpException(response.code(), "HTTP 요청 실패: " + response.code());
                }

                if (responseType == Void.class || responseType == void.class) {
                    return null;
                }

                String body = response.body() != null ? response.body().string() : "";
                if (body.isEmpty()) {
                    return null;
                }
                return JsonConverter.fromJson(body, responseType);
            } catch (IOException e) {
                lastException = e;
                attempts++;
                if (attempts <= retryCount) {
                    try {
                        Thread.sleep((long) Math.pow(2, attempts) * 100); // 지수 백오프
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("요청 중단됨", ie);
                    }
                }
            }
        }

        throw lastException;
    }

    private OkHttpClient buildClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(SHARED_CONNECTION_POOL)
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }

        return builder.build();
    }

    @Override
    public void close() {
        OkHttpClient c = this.client;
        if (c != null) {
            c.dispatcher().executorService().shutdown();
            c.connectionPool().evictAll();
            this.client = null;
        }
    }

    /**
     * HTTP 예외
     */
    public static class HttpException extends IOException {
        private final int statusCode;

        public HttpException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
