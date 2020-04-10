package org.jetlinks.demo.protocol.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.message.codec.http.HttpExchangeMessage;
import org.jetlinks.core.message.codec.http.SimpleHttpResponseMessage;
import org.jetlinks.demo.protocol.TopicMessageCodec;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


public class HttpDeviceMessageCodec extends TopicMessageCodec implements DeviceMessageCodec {

    public Transport getSupportTransport() {
        return DefaultTransport.HTTP;
    }

    @Override
    public Mono<? extends Message> decode(MessageDecodeContext context) {

        return Mono.defer(() -> {
            HttpExchangeMessage mqttMessage = (HttpExchangeMessage) context.getMessage();

            String topic = mqttMessage.getUrl();
            JSONObject payload = JSON.parseObject(mqttMessage.getPayload().toString(StandardCharsets.UTF_8));


            return Mono.justOrEmpty(doDecode(null, topic, payload))
                    .switchIfEmpty(Mono.defer(() -> {
                        //未转换成功，响应404
                        return mqttMessage.response(SimpleHttpResponseMessage
                                .builder()
                                .status(404)
                                .contentType(MediaType.APPLICATION_JSON)
                                .payload(Unpooled.wrappedBuffer("{\"success\":false}".getBytes()))
                                .build()).then(Mono.empty());
                    }))
                    .flatMap(msg -> {
                        //响应成功
                        return mqttMessage.response(SimpleHttpResponseMessage
                                .builder()
                                .status(200)
                                .contentType(MediaType.APPLICATION_JSON)
                                .payload(Unpooled.wrappedBuffer("{\"success\":true}".getBytes()))
                                .build())
                                .thenReturn(msg);
                    });
        });
    }

    public Mono<EncodedMessage> encode(MessageEncodeContext context) {
        return Mono.empty();
    }


}
