package org.jetlinks.demo.protocol.tcp.client;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import lombok.Setter;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.message.codec.DefaultTransport;
import org.jetlinks.core.message.codec.EncodedMessage;
import org.jetlinks.core.message.codec.Transport;
import org.jetlinks.core.server.session.DeviceSession;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

public class TcpClientDeviceSession implements DeviceSession {

    private final DeviceOperator deviceOperator;

    private final NetSocket netSocket;

    private long lastPingTime = System.currentTimeMillis();

    private final long connectTime = System.currentTimeMillis();

    @Setter
    private boolean closed;

    private long keepaliveTimeout = Duration.ofMinutes(10).toMillis();

    public TcpClientDeviceSession(DeviceOperator deviceOperator, NetSocket netSocket) {
        this.deviceOperator = deviceOperator;
        this.netSocket = netSocket;
    }

    @Override
    public String getId() {
        return getDeviceId();
    }

    @Override
    public String getDeviceId() {
        return deviceOperator.getDeviceId();
    }

    @Nullable
    @Override
    public DeviceOperator getOperator() {
        return deviceOperator;
    }

    @Override
    public long lastPingTime() {
        return lastPingTime;
    }

    @Override
    public long connectTime() {
        return connectTime;
    }

    @Override
    public Mono<Boolean> send(EncodedMessage encodedMessage) {
        return Mono.create(sink -> netSocket.write(Buffer.buffer(encodedMessage.getPayload()), async -> {
            if (async.succeeded()) {
                sink.success(true);
            } else {
                sink.error(async.cause());
            }
        }));
    }

    @Override
    public Transport getTransport() {
        return DefaultTransport.TCP;
    }

    @Override
    public void close() {

    }

    @Override
    public void ping() {
        lastPingTime = System.currentTimeMillis();
    }

    @Override
    public Optional<InetSocketAddress> getClientAddress() {
        return Optional
            .ofNullable(netSocket.remoteAddress())
            .map(addr -> new InetSocketAddress(addr.host(), addr.port()));
    }

    @Override
    public void setKeepAliveTimeout(Duration timeout) {
        keepaliveTimeout = timeout.toMillis();
    }

    @Override
    public boolean isAlive() {
        return !closed && System.currentTimeMillis() - lastPingTime < keepaliveTimeout;
    }

    @Override
    public void onClose(Runnable call) {

    }
}
