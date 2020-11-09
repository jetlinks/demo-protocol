package org.jetlinks.demo.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.codec.binary.Hex;
import org.jetlinks.core.message.codec.EncodedMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.demo.protocol.tcp.message.AuthRequest;
import org.jetlinks.demo.protocol.tcp.message.FireAlarm;
import org.jetlinks.demo.protocol.tcp.message.ReadProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

class DemoTcpMessageTest {

    @Test
    void test() {
        DemoTcpMessage message = DemoTcpMessage.of(MessageType.AUTH_REQ, AuthRequest.of(1000, "admin"));

        byte[] data = message.toBytes();
        System.out.println(Hex.encodeHexString(data));

        DemoTcpMessage decode = DemoTcpMessage.of(data);

        System.out.println(decode);

        Assertions.assertEquals(message.getType(), decode.getType());
        Assertions.assertArrayEquals(message.getData().toBytes(), decode.getData().toBytes());


    }

    @Test
    void encodeTest() {
        DemoTcpMessageCodec demoTcpMessageCodec = new DemoTcpMessageCodec(null);
        ReadPropertyMessage readPropertyMessage = new ReadPropertyMessage();
        readPropertyMessage.setCode("10001");
        readPropertyMessage.setDeviceId("1000");
        readPropertyMessage.setMessageId("test");
        readPropertyMessage.setTimestamp(LocalDateTime.now().getNano());
        DemoTcpMessage of = DemoTcpMessage.of(MessageType.READ_PROPERTY, ReadProperty.of(readPropertyMessage));
        EncodedMessage simple = EncodedMessage.simple(of.toByteBuf());
        ByteBuf byteBuf = simple.getPayload();
        byte[] payload = ByteBufUtil.getBytes(byteBuf, 0, byteBuf.readableBytes(), false);
        DemoTcpMessage message = DemoTcpMessage.of(payload);
        System.out.println(message.getType().getText());
        System.out.println(Hex.encodeHexString(payload));
        ReadProperty data = (ReadProperty) message.getData();
        System.out.println(data.getReadPropertyMessage().toString());
    }

    @Test
    void encodeEvent() {
        DemoTcpMessage demoTcpMessage = DemoTcpMessage.of(MessageType.FIRE_ALARM,
                FireAlarm.builder()
                        .point(ThreadLocalRandom.current().nextInt())
                        .lat(36.5F)
                        .lnt(122.3F)
                        .deviceId(1000)
                        .bName("a")
                        .build());
        byte[] data = new byte[500*1024];
        System.out.println(demoTcpMessage);
        System.out.println(Hex.encodeHexString(data));
        //061400000000000000000003e842f43e7742cc77cf1bd9071c
        //\06\14\00\00\00\00\00\00\00\00\00\03\e8B\f4>wB\ccw\cf\nt\c6\1a \06\14\00\00\00\00\00\00\00\00\00\03\e8B\f4>wB\ccw\cf\1b\d9\07\1c
        //\06\14\00\00\00\00\00\00\00\00\00\03\e8B\f4>wB\ccw\cf\ba\f9#\aa
    }


}