package org.jetlinks.demo.protocol.coap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.californium.core.coap.CoAP;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.demo.protocol.TopicMessageCodec;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


public class CoAPDeviceMessageCodec extends TopicMessageCodec implements DeviceMessageCodec {

    public Transport getSupportTransport() {
        return DefaultTransport.CoAP;
    }

    @Override
    public Mono<? extends Message> decode(MessageDecodeContext context) {

        return Mono.defer(() -> {
            //CoAP消息
            CoapExchangeMessage message = (CoapExchangeMessage) context.getMessage();

            //path 当成topic
            String topic = message.getPath();
            if (!topic.startsWith("/")) {
                topic = "/".concat(topic);
            }
            //转为json
            JSONObject payload = message.payloadAsJson();

            //解码消息
            return Mono
                    .justOrEmpty(doDecode(null, topic, payload))
                    .doOnSuccess(msg -> {
                        if (msg == null) {
                            //响应成功消息
                            message.getExchange()
                                    .respond("success");
                        } else {
                            //响应4.04
                            message.getExchange()
                                    .respond(CoAP.ResponseCode.NOT_FOUND);

                        }
                    });
        });
    }

    public Mono<EncodedMessage> encode(MessageEncodeContext context) {
        return Mono.empty();

    }


}
