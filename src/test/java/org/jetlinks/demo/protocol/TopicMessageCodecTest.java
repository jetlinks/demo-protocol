package org.jetlinks.demo.protocol;

import com.alibaba.fastjson.JSONObject;
import org.jetlinks.core.message.ChildDeviceMessage;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.demo.protocol.mqtt.MqttDeviceMessageCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicMessageCodecTest {

    @Test
    void testChildrenMessage() {
        TopicMessageCodec codec = new MqttDeviceMessageCodec();

        DeviceMessage message = codec.doDecode("test", "/children/fire_alarm", new JSONObject());

        assertTrue(message instanceof ChildDeviceMessage);
        ChildDeviceMessage msg = ((ChildDeviceMessage) message);
        assertTrue(msg.getChildDeviceMessage() instanceof EventMessage);

    }

}