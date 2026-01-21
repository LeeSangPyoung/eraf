package com.eraf.starter.ftp.impl;

import com.eraf.starter.ftp.ErafFtpProperties;
import com.eraf.starter.ftp.FtpClient;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Apache Commons Net FTP 클라이언트 구현
 */
public class FtpClientImpl implements FtpClient {

    private final ErafFtpProperties properties;
    private final FTPClient ftpClient;

    public FtpClientImpl(ErafFtpProperties properties) {
        this.properties = properties;
        this.ftpClient = new FTPClient();
    }

    @Override
    public void connect() {
        try {
            ftpClient.setConnectTimeout(properties.getConnectionTimeout());
            ftpClient.setDataTimeout(java.time.Duration.ofMillis(properties.getDataTimeout()));
            ftpClient.connect(properties.getHost(), properties.getPort());

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new RuntimeException("FTP server refused connection: " + reply);
            }

            if (!ftpClient.login(properties.getUsername(), properties.getPassword())) {
                throw new RuntimeException("FTP login failed");
            }

            if (properties.isPassiveMode()) {
                ftpClient.enterLocalPassiveMode();
            }

            if (properties.isBinaryMode()) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to FTP server", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            // Ignore disconnect errors
        }
    }

    @Override
    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    @Override
    public boolean upload(String remotePath, InputStream inputStream) {
        try {
            return ftpClient.storeFile(remotePath, inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + remotePath, e);
        }
    }

    @Override
    public boolean upload(String remotePath, byte[] data) {
        return upload(remotePath, new ByteArrayInputStream(data));
    }

    @Override
    public InputStream download(String remotePath) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (!ftpClient.retrieveFile(remotePath, outputStream)) {
                throw new RuntimeException("Failed to download file: " + remotePath);
            }
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + remotePath, e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String remotePath) {
        try (InputStream is = download(remotePath)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + remotePath, e);
        }
    }

    @Override
    public boolean delete(String remotePath) {
        try {
            return ftpClient.deleteFile(remotePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + remotePath, e);
        }
    }

    @Override
    public List<String> list(String remotePath) {
        try {
            FTPFile[] files = ftpClient.listFiles(remotePath);
            return Arrays.stream(files)
                    .map(FTPFile::getName)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files: " + remotePath, e);
        }
    }

    @Override
    public boolean mkdir(String remotePath) {
        try {
            return ftpClient.makeDirectory(remotePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + remotePath, e);
        }
    }

    @Override
    public boolean exists(String remotePath) {
        try {
            FTPFile[] files = ftpClient.listFiles(remotePath);
            return files != null && files.length > 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean rename(String fromPath, String toPath) {
        try {
            return ftpClient.rename(fromPath, toPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rename file: " + fromPath + " -> " + toPath, e);
        }
    }
}
