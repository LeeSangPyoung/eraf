package com.eraf.starter.tcp.impl;

import com.eraf.starter.tcp.ErafTcpProperties;
import com.eraf.starter.tcp.TcpClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Netty 기반 TCP 클라이언트 구현
 */
public class NettyTcpClient implements TcpClient {

    private final ErafTcpProperties properties;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private Bootstrap bootstrap;

    private Consumer<byte[]> messageListener;
    private Consumer<ConnectionState> stateListener;
    private final AtomicReference<ConnectionState> currentState = new AtomicReference<>(ConnectionState.DISCONNECTED);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private BlockingQueue<byte[]> responseQueue;

    public NettyTcpClient(ErafTcpProperties properties) {
        this.properties = properties;
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        updateState(ConnectionState.CONNECTING);

        int threads = properties.getWorkerThreads() > 0 ? properties.getWorkerThreads() : 0;
        workerGroup = new NioEventLoopGroup(threads);

        bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, properties.isKeepAlive())
                .option(ChannelOption.TCP_NODELAY, properties.isTcpNoDelay())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectionTimeout())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ReadTimeoutHandler(properties.getReadTimeout(), TimeUnit.MILLISECONDS));
                        pipeline.addLast(new ClientHandler());
                    }
                });

        bootstrap.connect(properties.getHost(), properties.getPort())
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        channel = channelFuture.channel();
                        reconnectAttempts.set(0);
                        updateState(ConnectionState.CONNECTED);
                        future.complete(null);
                    } else {
                        updateState(ConnectionState.DISCONNECTED);
                        future.completeExceptionally(channelFuture.cause());
                        scheduleReconnect();
                    }
                });

        return future;
    }

    @Override
    public void disconnect() {
        updateState(ConnectionState.DISCONNECTING);

        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        updateState(ConnectionState.DISCONNECTED);
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    @Override
    public CompletableFuture<Void> send(byte[] data) {
        if (!isConnected()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Not connected"));
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        ByteBuf buf = Unpooled.wrappedBuffer(data);

        channel.writeAndFlush(buf).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(channelFuture.cause());
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<Void> send(String message) {
        return send(message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public byte[] sendAndReceive(byte[] data, long timeoutMillis) {
        responseQueue.clear();
        send(data).join();

        try {
            byte[] response = responseQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new TimeoutException("No response received within timeout");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for response", e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Consumer<byte[]> listener) {
        this.messageListener = listener;
    }

    @Override
    public void onConnectionStateChange(Consumer<ConnectionState> listener) {
        this.stateListener = listener;
    }

    private void updateState(ConnectionState newState) {
        currentState.set(newState);
        if (stateListener != null) {
            stateListener.accept(newState);
        }
    }

    private void scheduleReconnect() {
        if (!properties.isAutoReconnect()) {
            return;
        }

        int attempts = reconnectAttempts.incrementAndGet();
        if (attempts > properties.getMaxReconnectAttempts()) {
            return;
        }

        updateState(ConnectionState.RECONNECTING);

        workerGroup.schedule(() -> {
            connect();
        }, properties.getReconnectInterval(), TimeUnit.MILLISECONDS);
    }

    private class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);

                // 동기 송수신용 큐에 추가
                responseQueue.offer(data);

                // 리스너에 전달
                if (messageListener != null) {
                    messageListener.accept(data);
                }
            } finally {
                buf.release();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            updateState(ConnectionState.DISCONNECTED);
            scheduleReconnect();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
