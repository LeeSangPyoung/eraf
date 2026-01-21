package com.eraf.starter.s3;

import java.io.InputStream;
import java.util.List;

/**
 * 통합 파일 저장소 서비스 인터페이스
 */
public interface StorageService {

    /**
     * 파일 업로드
     *
     * @param path 저장 경로
     * @param inputStream 파일 스트림
     * @param contentType 콘텐츠 타입
     * @return 저장된 파일 경로
     */
    String upload(String path, InputStream inputStream, String contentType);

    /**
     * 파일 업로드 (바이트 배열)
     *
     * @param path 저장 경로
     * @param data 파일 데이터
     * @param contentType 콘텐츠 타입
     * @return 저장된 파일 경로
     */
    String upload(String path, byte[] data, String contentType);

    /**
     * 파일 다운로드
     *
     * @param path 파일 경로
     * @return 파일 스트림
     */
    InputStream download(String path);

    /**
     * 파일 다운로드 (바이트 배열)
     *
     * @param path 파일 경로
     * @return 파일 데이터
     */
    byte[] downloadAsBytes(String path);

    /**
     * 파일 삭제
     *
     * @param path 파일 경로
     */
    void delete(String path);

    /**
     * 파일 존재 여부 확인
     *
     * @param path 파일 경로
     * @return 존재 여부
     */
    boolean exists(String path);

    /**
     * 파일 목록 조회
     *
     * @param prefix 경로 접두사
     * @return 파일 경로 목록
     */
    List<String> list(String prefix);

    /**
     * Presigned URL 생성 (다운로드용)
     *
     * @param path 파일 경로
     * @param expirationMinutes 만료 시간(분)
     * @return Presigned URL
     */
    String getPresignedUrl(String path, int expirationMinutes);

    /**
     * Presigned URL 생성 (업로드용)
     *
     * @param path 파일 경로
     * @param expirationMinutes 만료 시간(분)
     * @return Presigned URL
     */
    String getPresignedUploadUrl(String path, int expirationMinutes);

    /**
     * 파일 복사
     *
     * @param sourcePath 원본 경로
     * @param targetPath 대상 경로
     */
    void copy(String sourcePath, String targetPath);

    /**
     * 파일 이동
     *
     * @param sourcePath 원본 경로
     * @param targetPath 대상 경로
     */
    void move(String sourcePath, String targetPath);
}
