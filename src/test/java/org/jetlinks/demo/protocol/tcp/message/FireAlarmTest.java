package org.jetlinks.demo.protocol.tcp.message;

import org.apache.commons.codec.binary.Hex;
import org.hswebframework.web.id.IDGenerator;
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
}