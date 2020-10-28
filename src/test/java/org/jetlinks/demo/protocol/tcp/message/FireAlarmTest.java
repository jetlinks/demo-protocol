package org.jetlinks.demo.protocol.tcp.message;

import org.apache.commons.codec.binary.Hex;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.demo.protocol.tcp.DemoTcpMessage;
import org.jetlinks.demo.protocol.tcp.MessageType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class FireAlarmTest {


    @Test
    void test() {

        FireAlarm alarm = FireAlarm.builder()
                .deviceId(IDGenerator.SNOW_FLAKE.generate())
                .lat(ThreadLocalRandom.current().nextFloat())
                .lnt(ThreadLocalRandom.current().nextFloat())
                .point(ThreadLocalRandom.current().nextInt())
                .build();

        byte[] bytes = alarm.toBytes();
        System.out.println(Hex.encodeHexString(bytes));

        FireAlarm decode=new FireAlarm();
        decode.fromBytes(bytes,0);
        System.out.println(decode);
        assertEquals(alarm,decode);

    }
    @Test
    void encodeEvent() {
        DemoTcpMessage demoTcpMessage = DemoTcpMessage.of(MessageType.FIRE_ALARM,
                FireAlarm.builder()
                        .point(ThreadLocalRandom.current().nextInt())
                        .lat(36.5F)
                        .lnt(122.3F)
                        .deviceId(1000)
                        .build());
        byte[] data = demoTcpMessage.toBytes();
        System.out.println(demoTcpMessage);
        System.out.println(Hex.encodeHexString(data));
        System.out.println("0614000000e8030000000000009a99f4420000124222b7c94c");
    }
}