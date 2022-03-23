package org.jetlinks.demo.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.jetlinks.core.message.codec.EncodedMessage;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.demo.protocol.tcp.message.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    void encodeCustomTest() {
        ReportPropertyMessage readPropertyMessage = new ReportPropertyMessage();
        Map<String, Object> map = new HashMap<>();
        map.put("name", RandomStringUtils.randomAlphanumeric(10));
        map.put("A1", (int) (Math.random() * 100));
        map.put("temperature", Math.random() * 100);
        readPropertyMessage.setDeviceId("1000");
        readPropertyMessage.setProperties(map);
        DemoTcpMessage of = DemoTcpMessage.of(MessageType.REPORT_PROPERTY, ReportProperty.of(readPropertyMessage));
        EncodedMessage simple = EncodedMessage.simple(of.toByteBuf());
        ByteBuf byteBuf = simple.getPayload();
        byte[] payload = ByteBufUtil.getBytes(byteBuf, 0, byteBuf.readableBytes(), false);
        DemoTcpMessage message = DemoTcpMessage.of(payload);
        System.out.println(message.getType().getText());
        System.out.println(Hex.encodeHexString(payload));
        ReportProperty data = (ReportProperty) message.getData();
        System.out.println(data.getReportPropertyMessage().toString());
    }

    @Test
    void encodeReport() {
        float temp = (float) (Math.random() * 1000);
        ReportPropertyMessage report = new ReportPropertyMessage();
        report.setProperties(Collections.singletonMap("temperature", temp));
        report.setDeviceId("1000");
        report.setMessageId("test");
        report.setTimestamp(LocalDateTime.now().getNano());
        DemoTcpMessage of = DemoTcpMessage.of(MessageType.REPORT_TEMPERATURE, TemperatureReport.of(1000l, temp));
        EncodedMessage simple = EncodedMessage.simple(of.toByteBuf());
        ByteBuf byteBuf = simple.getPayload();
        byte[] payload = ByteBufUtil.getBytes(byteBuf, 0, byteBuf.readableBytes(), false);
        DemoTcpMessage demoTcpMessage = DemoTcpMessage.of(payload);
        System.out.println(demoTcpMessage.getType().getText());
        System.out.println(Hex.encodeHexString(payload));
        TcpDeviceMessage data = (TcpDeviceMessage) demoTcpMessage.getData();
        System.out.println(data.toDeviceMessage());
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
        byte[] data = demoTcpMessage.toBytes();
        System.out.println(demoTcpMessage);
        System.out.println(Hex.encodeHexString(data));
        //061400000000000000000003e842f43e7742cc77cf1bd9071c
        //\06\14\00\00\00\00\00\00\00\00\00\03\e8B\f4>wB\ccw\cf\nt\c6\1a \06\14\00\00\00\00\00\00\00\00\00\03\e8B\f4>wB\ccw\cf\1b\d9\07\1c
        //\06\14\00\00\00\00\00\00\00\00\00\03\e8B\f4>wB\ccw\cf\ba\f9#\aa
    }


}