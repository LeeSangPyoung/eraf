package com.eraf.starter.ftp;

import java.io.InputStream;
import java.util.List;

/**
 * FTP/SFTP 클라이언트 인터페이스
 */
public interface FtpClient {

    /**
     * 연결
     */
    void connect();

    /**
     * 연결 해제
     */
    void disconnect();

    /**
     * 연결 상태 확인
     */
    boolean isConnected();

    /**
     * 파일 업로드
     */
    boolean upload(String remotePath, InputStream inputStream);

    /**
     * 파일 업로드 (바이트 배열)
     */
    boolean upload(String remotePath, byte[] data);

    /**
     * 파일 다운로드
     */
    InputStream download(String remotePath);

    /**
     * 파일 다운로드 (바이트 배열)
     */
    byte[] downloadAsBytes(String remotePath);

    /**
     * 파일 삭제
     */
    boolean delete(String remotePath);

    /**
     * 파일 목록 조회
     */
    List<String> list(String remotePath);

    /**
     * 디렉토리 생성
     */
    boolean mkdir(String remotePath);

    /**
     * 파일 존재 여부 확인
     */
    boolean exists(String remotePath);

    /**
     * 파일 이동/이름 변경
     */
    boolean rename(String fromPath, String toPath);
}
