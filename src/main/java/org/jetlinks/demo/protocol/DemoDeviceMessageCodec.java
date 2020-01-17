package org.jetlinks.demo.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.codec.*;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessage;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


public class DemoDeviceMessageCodec extends DemoTopicMessageCodec implements DeviceMessageCodec {
    public Transport getSupportTransport() {
        return DefaultTransport.MQTT;
    }

    @Override
    public Mono<? extends Message> decode(MessageDecodeContext context) {

        return Mono.fromSupplier(() -> {
            MqttMessage mqttMessage = (MqttMessage) context.getMessage();

            String topic = mqttMessage.getTopic();
            JSONObject payload = JSON.parseObject(mqttMessage.getPayload().toString(StandardCharsets.UTF_8));

            return doEncode(topic, payload);
        });
    }


    public Mono<EncodedMessage> encode(MessageEncodeContext context) {
        Message message = context.getMessage();
        return Mono.fromSupplier(() -> {
            if (message instanceof ReadPropertyMessage) {
                String topic = "/read-property";
                JSONObject mqttData = new JSONObject();
                mqttData.put("messageId", message.getMessageId());
                mqttData.put("deviceId", ((ReadPropertyMessage) message).getDeviceId());
                mqttData.put("properties", ((ReadPropertyMessage) message).getProperties());
                return SimpleMqttMessage.builder()
                        .topic(topic)
                        .payload(Unpooled.copiedBuffer(JSON.toJSONBytes(mqttData)))
                        .build();
            } else if (message instanceof WritePropertyMessage) {
                String topic = "/write-property";
                JSONObject mqttData = new JSONObject();
                mqttData.put("messageId", message.getMessageId());
                mqttData.put("deviceId", ((WritePropertyMessage) message).getDeviceId());
                mqttData.put("properties", ((WritePropertyMessage) message).getProperties());
                return SimpleMqttMessage.builder()
                        .topic(topic)
                        .payload(Unpooled.copiedBuffer(JSON.toJSONBytes(mqttData)))
                        .build();
            } else if (message instanceof FunctionInvokeMessage) {
                String topic = "/invoke-function";
                FunctionInvokeMessage invokeMessage = ((FunctionInvokeMessage) message);
                JSONObject mqttData = new JSONObject();
                mqttData.put("messageId", message.getMessageId());
                mqttData.put("deviceId", ((FunctionInvokeMessage) message).getDeviceId());
                mqttData.put("function", invokeMessage.getFunctionId());
                mqttData.put("args", invokeMessage.getInputs());
                return SimpleMqttMessage.builder()
                        .topic(topic)
                        .payload(Unpooled.copiedBuffer(JSON.toJSONBytes(mqttData)))
                        .build();
            }
            return null;
        });

    }

}
