package com.eraf.core.file;

/**
 * 파일을 찾을 수 없을 때 발생하는 예외
 */
public class FileNotFoundException extends FileStorageException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
