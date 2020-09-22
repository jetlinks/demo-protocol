package org.jetlinks.demo.protocol.tcp.client;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.AllArgsConstructor;
import org.jetlinks.core.Value;
import org.jetlinks.core.device.DeviceOperator;
import org.jetlinks.core.device.DeviceState;
import org.jetlinks.core.device.DeviceStateChecker;
import org.jetlinks.core.server.session.DeviceSession;
import org.jetlinks.core.server.session.DeviceSessionManager;
import org.jetlinks.supports.server.DecodedClientMessageHandler;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
public class TcpClientMessageSupport implements Disposable, DeviceStateChecker {

    private final DecodedClientMessageHandler messageHandler;

    private final DeviceSessionManager sessionManager;

    private final Vertx vertx;

    public Mono<DeviceSession> getOrCreateSession(DeviceOperator deviceOperator) {
        DeviceSession session = sessionManager.getSession(deviceOperator.getDeviceId());
        if (session != null) {
            return Mono.just(session);
        }

        return deviceOperator
            .getSelfConfigs("host", "port")
            .flatMap(values -> {
                String host = values.getValue("host").map(Value::asString).orElse(null);
                int port = values.getValue("port").map(Value::asInt).orElse(0);
                if (StringUtils.isEmpty(host) || port == 0) {
                    return Mono.empty();
                }

                // TODO: 2020/9/22 重试连接逻辑实现
                return Mono.create(sink -> vertx
                    .createNetClient()
                    .connect(port, host, async -> {
                        if (async.succeeded()) {
                            DeviceSession deviceSession = new TcpClientDeviceSession(deviceOperator, async.result());
                            handleSocket(async.result(), deviceOperator);

                            sessionManager.register(deviceSession);
                            sink.success(deviceSession);
                        } else {
                            sink.error(async.cause());
                        }
                    }));
            });

    }

    public void handleSocket(NetSocket netSocket, DeviceOperator deviceOperator) {

        netSocket
            .handler(buffer -> {
                // TODO: 2020/9/22 自定义编解码

                //解码后发送给平台
//                messageHandler
//                    .handleMessage(deviceOperator, message)
//                    .subscribe();
            });
    }

    @Override
    public @NotNull Mono<Byte> checkState(@NotNull DeviceOperator device) {
        return getOrCreateSession(device)
            .map(session -> session.isAlive() ? DeviceState.online : DeviceState.offline)
            .defaultIfEmpty(DeviceState.offline);
    }

    @Override
    public void dispose() {

    }
}
