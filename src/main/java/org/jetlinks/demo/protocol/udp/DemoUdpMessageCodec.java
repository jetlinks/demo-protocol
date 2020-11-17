package org.jetlinks.demo.protocol.udp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.core.Value;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.server.session.DeviceSession;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.demo.protocol.tcp.DemoTcpMessage;
import org.jetlinks.demo.protocol.tcp.MessageType;
import org.jetlinks.demo.protocol.tcp.TcpStatus;
import org.jetlinks.demo.protocol.tcp.message.AuthResponse;
import org.jetlinks.demo.protocol.tcp.message.ErrorMessage;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author wangzheng
 * @see
 * @since 1.0
 */
@AllArgsConstructor
@Slf4j
public class DemoUdpMessageCodec implements DeviceMessageCodec {

    @Override
    public Transport getSupportTransport() {
        return DefaultTransport.UDP;
    }

    @Override
    public Flux<DeviceMessage> decode(MessageDecodeContext context) {
        return Flux.defer(() -> {
            FromDeviceMessageContext ctx = ((FromDeviceMessageContext) context);
            DeviceSession session=ctx.getSession();
            EncodedMessage encodedMessage = context.getMessage();
            JSONObject payload = JSON.parseObject(encodedMessage.getPayload().toString(StandardCharsets.UTF_8));
            return Mono
                    .justOrEmpty(org.jetlinks.core.message.MessageType.<DeviceMessage>convertMessage(payload))
                    .flatMapMany(msg->{
                       return context
                                .getDevice(msg.getDeviceId())
                                .flatMapMany(operator -> operator.getConfig("udp_auth_key")
                                        .map(Value::asString)
                                        .filter(key -> key.equals(payload.getString("key")))
                                        .flatMapMany(ignore -> {
                                            //认证通过
                                            DeviceOnlineMessage onlineMessage = new DeviceOnlineMessage();
                                            onlineMessage.setDeviceId(operator.getDeviceId());
                                            onlineMessage.setTimestamp(System.currentTimeMillis());
                                            return session
                                                    .send(EncodedMessage.simple(Unpooled.wrappedBuffer(Response.of(MessageType.AUTH_RES, "SUCCESS").toString().getBytes())))
                                                    .thenMany(Flux.just(msg,onlineMessage));
                                        }));

                    }) .switchIfEmpty(Mono.defer(() -> session
                            .send(EncodedMessage.simple(Unpooled.wrappedBuffer(Response.of(MessageType.AUTH_RES, "ILLEGAL_ARGUMENTS").toString().getBytes())))
                            .then(Mono.empty())));


        });
    }

    @Override
    public Publisher<? extends EncodedMessage> encode(MessageEncodeContext context) {
        return Mono.empty();
    }

    @Setter
    @Getter
    @AllArgsConstructor(staticName = "of")
    private static class Response {
        private MessageType type;

        private Object res;

    }

}
