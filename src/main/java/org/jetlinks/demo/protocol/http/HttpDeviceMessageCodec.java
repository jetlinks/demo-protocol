package org.jetlinks.demo.protocol.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DisconnectDeviceMessage;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.message.codec.http.HttpExchangeMessage;
import org.jetlinks.core.message.codec.http.SimpleHttpResponseMessage;
import org.jetlinks.demo.protocol.DemoTopicMessageCodec;
import org.jetlinks.demo.protocol.TopicMessage;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


public class HttpDeviceMessageCodec extends DemoTopicMessageCodec implements DeviceMessageCodec {

    public Transport getSupportTransport() {
        return DefaultTransport.HTTP;
    }

    @Override
    public Mono<? extends Message> decode(MessageDecodeContext context) {

        return Mono.defer(() -> {
            HttpExchangeMessage mqttMessage = (HttpExchangeMessage) context.getMessage();

            String topic = mqttMessage.getUrl();
            JSONObject payload = JSON.parseObject(mqttMessage.getPayload().toString(StandardCharsets.UTF_8));

            String deviceId = context.getDevice() != null ? context.getDevice().getDeviceId() : null;

            return mqttMessage.response(SimpleHttpResponseMessage
                    .builder()
                    .status(200)
                    .contentType(MediaType.APPLICATION_JSON)
                    .payload(Unpooled.wrappedBuffer("{\"success\":true}".getBytes()))
                    .build())
                    .thenReturn(doDecode(deviceId, topic, payload));
        });
    }

    public Mono<EncodedMessage> encode(MessageEncodeContext context) {
        return Mono.empty();
    }


}
