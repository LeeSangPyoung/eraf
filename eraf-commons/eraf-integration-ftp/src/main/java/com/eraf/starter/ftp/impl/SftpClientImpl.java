package com.eraf.starter.ftp.impl;

import com.eraf.starter.ftp.ErafFtpProperties;
import com.eraf.starter.ftp.FtpClient;
import com.jcraft.jsch.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * JSch SFTP 클라이언트 구현
 */
public class SftpClientImpl implements FtpClient {

    private final ErafFtpProperties properties;
    private Session session;
    private ChannelSftp channelSftp;

    public SftpClientImpl(ErafFtpProperties properties) {
        this.properties = properties;
    }

    @Override
    public void connect() {
        try {
            JSch jsch = new JSch();

            // 개인키 인증
            if (properties.getPrivateKeyPath() != null) {
                if (properties.getPrivateKeyPassphrase() != null) {
                    jsch.addIdentity(properties.getPrivateKeyPath(), properties.getPrivateKeyPassphrase());
                } else {
                    jsch.addIdentity(properties.getPrivateKeyPath());
                }
            }

            session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());

            // 비밀번호 인증
            if (properties.getPassword() != null) {
                session.setPassword(properties.getPassword());
            }

            // Host key 검증 비활성화 (프로덕션에서는 known_hosts 사용 권장)
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.setTimeout(properties.getConnectionTimeout());
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

        } catch (JSchException e) {
            throw new RuntimeException("Failed to connect to SFTP server", e);
        }
    }

    @Override
    public void disconnect() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isConnected() &&
               channelSftp != null && channelSftp.isConnected();
    }

    @Override
    public boolean upload(String remotePath, InputStream inputStream) {
        try {
            channelSftp.put(inputStream, remotePath);
            return true;
        } catch (SftpException e) {
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
            return channelSftp.get(remotePath);
        } catch (SftpException e) {
            throw new RuntimeException("Failed to download file: " + remotePath, e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String remotePath) {
        try (InputStream is = download(remotePath);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + remotePath, e);
        }
    }

    @Override
    public boolean delete(String remotePath) {
        try {
            channelSftp.rm(remotePath);
            return true;
        } catch (SftpException e) {
            throw new RuntimeException("Failed to delete file: " + remotePath, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> list(String remotePath) {
        try {
            Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(remotePath);
            List<String> fileNames = new ArrayList<>();
            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (!".".equals(name) && !"..".equals(name)) {
                    fileNames.add(name);
                }
            }
            return fileNames;
        } catch (SftpException e) {
            throw new RuntimeException("Failed to list files: " + remotePath, e);
        }
    }

    @Override
    public boolean mkdir(String remotePath) {
        try {
            channelSftp.mkdir(remotePath);
            return true;
        } catch (SftpException e) {
            throw new RuntimeException("Failed to create directory: " + remotePath, e);
        }
    }

    @Override
    public boolean exists(String remotePath) {
        try {
            channelSftp.stat(remotePath);
            return true;
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw new RuntimeException("Failed to check file existence: " + remotePath, e);
        }
    }

    @Override
    public boolean rename(String fromPath, String toPath) {
        try {
            channelSftp.rename(fromPath, toPath);
            return true;
        } catch (SftpException e) {
            throw new RuntimeException("Failed to rename file: " + fromPath + " -> " + toPath, e);
        }
    }
}
