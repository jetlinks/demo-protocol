package org.jetlinks.demo.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.DisconnectDeviceMessage;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


public class MqttDeviceMessageCodec extends DemoTopicMessageCodec implements DeviceMessageCodec {
    public Transport getSupportTransport() {
        return DefaultTransport.MQTT;
    }

    @Override
    public Mono<? extends Message> decode(MessageDecodeContext context) {

        return Mono.fromSupplier(() -> {
            MqttMessage mqttMessage = (MqttMessage) context.getMessage();

            String topic = mqttMessage.getTopic();
            JSONObject payload = JSON.parseObject(mqttMessage.getPayload().toString(StandardCharsets.UTF_8));

            String deviceId = context.getDevice() != null ? context.getDevice().getDeviceId() : null;

            return doDecode(deviceId, topic, payload);
        });
    }

    public Mono<EncodedMessage> encode(MessageEncodeContext context) {
        Message message = context.getMessage();
        return Mono.defer(() -> {
            if (message instanceof DeviceMessage) {
                if (message instanceof DisconnectDeviceMessage) {
                    return ((ToDeviceMessageContext) context)
                            .disconnect()
                            .then(Mono.empty());
                }

                TopicMessage msg = doEncode((DeviceMessage) message);
                if (null == msg) {
                    return Mono.empty();
                }

                return Mono.just(SimpleMqttMessage.builder()
                                .topic(msg.getTopic())
                                .payload(Unpooled.wrappedBuffer(JSON.toJSONBytes(msg.getMessage())))
                                .build());
            }
            return Mono.empty();

        });

    }


}
