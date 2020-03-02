package org.jetlinks.demo.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.Value;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.server.session.DeviceSession;
import org.jetlinks.demo.protocol.tcp.message.AuthRequest;
import org.jetlinks.demo.protocol.tcp.message.AuthResponse;
import org.jetlinks.demo.protocol.tcp.message.ErrorMessage;
import org.jetlinks.demo.protocol.tcp.message.Pong;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@AllArgsConstructor
@Slf4j
public class DemoTcpMessageCodec implements DeviceMessageCodec {

    private DeviceRegistry registry;

    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.TCP;
    }

    @Override
    public Mono<DeviceMessage> decode(MessageDecodeContext context) {
        return Mono.defer(() -> {
            FromDeviceMessageContext ctx = ((FromDeviceMessageContext) context);
            ByteBuf byteBuf = context.getMessage().getPayload();
            byte[] payload = ByteBufUtil.getBytes(byteBuf, 0, byteBuf.readableBytes(), false);
            if (log.isDebugEnabled()) {
                log.debug("handle tcp message:\n{}", Hex.encodeHexString(payload));
            }
            DemoTcpMessage message;
            try {
                message = DemoTcpMessage.of(payload);
                if (log.isDebugEnabled()) {
                    log.debug("decode tcp message:\n{}\n{}", Hex.encodeHexString(payload), message);
                }
            } catch (Exception e) {
                log.warn("decode tcp message error:[{}]", Hex.encodeHexString(payload), e);
                return Mono.error(e);
            }
            DeviceSession session = ctx.getSession();
            if (session.getOperator() == null) {
                //设备没有认证就发送了消息
                if (message.getType() != MessageType.AUTH_REQ) {
                    log.warn("tcp session[{}], unauthorized.", session.getId());
                    return session
                            .send(EncodedMessage.simple(DemoTcpMessage.of(MessageType.ERROR, ErrorMessage.of(TcpStatus.UN_AUTHORIZED)).toByteBuf()))
                            .then(Mono.fromRunnable(session::close));
                }
                AuthRequest request = ((AuthRequest) message.getData());
                String deviceId = buildDeviceId(request.getDeviceId());
                return registry
                        .getDevice(buildDeviceId(request.getDeviceId()))
                        .flatMap(operator -> operator.getConfig("tcp_auth_key")
                                .map(Value::asString)
                                .filter(key -> Arrays.equals(request.getKey(), key.getBytes()))
                                .flatMap(msg -> {
                                    //认证通过
                                    DeviceOnlineMessage onlineMessage = new DeviceOnlineMessage();
                                    onlineMessage.setDeviceId(deviceId);
                                    onlineMessage.setTimestamp(System.currentTimeMillis());
                                    return session
                                            .send(EncodedMessage.simple(DemoTcpMessage.of(MessageType.AUTH_RES, AuthResponse.of(request.getDeviceId(), TcpStatus.SUCCESS)).toByteBuf()))
                                            .thenReturn(onlineMessage);
                                }))
                        //为空可能设备不存在或者没有配置tcp_auth_key,响应错误信息.
                        .switchIfEmpty(Mono.defer(() -> session
                                .send(EncodedMessage.simple(
                                        DemoTcpMessage.of(MessageType.AUTH_RES, AuthResponse.of(request.getDeviceId(), TcpStatus.ILLEGAL_ARGUMENTS)).toByteBuf()))
                                .then(Mono.empty())));
            }
            //keepalive, ping pong
            if (message.getType() == MessageType.PING) {
                return session
                        .send(EncodedMessage.simple(Unpooled.wrappedBuffer(DemoTcpMessage.of(MessageType.PONG, new Pong()).toBytes())))
                        .then(Mono.fromRunnable(session::ping));
            }
            if (message.getData() instanceof TcpDeviceMessage) {
                return Mono.justOrEmpty(((TcpDeviceMessage) message.getData()).toDeviceMessage());
            }
            return Mono.empty();
        });
    }

    public String buildDeviceId(long deviceId) {
        return String.valueOf(deviceId);
    }

    @Override
    public Publisher<? extends EncodedMessage> encode(MessageEncodeContext context) {
        //暂不支持
        return Mono.empty();
    }
}
