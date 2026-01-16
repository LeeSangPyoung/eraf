package com.eraf.core.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * 파일 저장소 서비스 인터페이스
 * 로컬, S3, FTP 등 다양한 구현체 지원
 */
public interface FileStorageService {

    /**
     * 파일 저장
     *
     * @param file 업로드 파일
     * @return 저장된 파일 정보
     */
    StoredFile store(MultipartFile file);

    /**
     * 파일 저장 (지정된 경로)
     *
     * @param file     업로드 파일
     * @param directory 저장 디렉토리
     * @return 저장된 파일 정보
     */
    StoredFile store(MultipartFile file, String directory);

    /**
     * 파일 저장 (커스텀 파일명)
     *
     * @param file      업로드 파일
     * @param directory 저장 디렉토리
     * @param filename  저장할 파일명
     * @return 저장된 파일 정보
     */
    StoredFile store(MultipartFile file, String directory, String filename);

    /**
     * InputStream으로 파일 저장
     *
     * @param inputStream 입력 스트림
     * @param directory   저장 디렉토리
     * @param filename    파일명
     * @param contentType 컨텐츠 타입
     * @return 저장된 파일 정보
     */
    StoredFile store(InputStream inputStream, String directory, String filename, String contentType);

    /**
     * 여러 파일 저장
     *
     * @param files 업로드 파일 목록
     * @return 저장된 파일 정보 목록
     */
    List<StoredFile> storeAll(List<MultipartFile> files);

    /**
     * 여러 파일 저장 (지정된 경로)
     *
     * @param files     업로드 파일 목록
     * @param directory 저장 디렉토리
     * @return 저장된 파일 정보 목록
     */
    List<StoredFile> storeAll(List<MultipartFile> files, String directory);

    /**
     * 파일 로드
     *
     * @param filePath 파일 경로
     * @return Resource
     */
    Resource load(String filePath);

    /**
     * 파일 경로 반환
     *
     * @param filePath 상대 파일 경로
     * @return 절대 파일 경로
     */
    Path getPath(String filePath);

    /**
     * 파일 존재 여부 확인
     *
     * @param filePath 파일 경로
     * @return 존재 여부
     */
    boolean exists(String filePath);

    /**
     * 파일 삭제
     *
     * @param filePath 파일 경로
     * @return 삭제 성공 여부
     */
    boolean delete(String filePath);

    /**
     * 여러 파일 삭제
     *
     * @param filePaths 파일 경로 목록
     * @return 삭제된 파일 수
     */
    int deleteAll(List<String> filePaths);

    /**
     * 디렉토리 내 모든 파일 삭제
     *
     * @param directory 디렉토리 경로
     * @return 삭제된 파일 수
     */
    int deleteDirectory(String directory);

    /**
     * 파일 복사
     *
     * @param sourcePath      원본 경로
     * @param destinationPath 대상 경로
     * @return 복사 성공 여부
     */
    boolean copy(String sourcePath, String destinationPath);

    /**
     * 파일 이동
     *
     * @param sourcePath      원본 경로
     * @param destinationPath 대상 경로
     * @return 이동 성공 여부
     */
    boolean move(String sourcePath, String destinationPath);

    /**
     * 파일 URL 반환
     *
     * @param filePath 파일 경로
     * @return 파일 접근 URL
     */
    String getUrl(String filePath);
}
