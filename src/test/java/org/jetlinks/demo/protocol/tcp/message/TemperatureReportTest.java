package org.jetlinks.demo.protocol.tcp.message;

import org.apache.commons.codec.binary.Hex;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.demo.protocol.tcp.DemoTcpMessage;
import org.jetlinks.demo.protocol.tcp.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.IdGenerator;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureReportTest {


    @Test
    void test() {
        TemperatureReport report = TemperatureReport.of(1000L, 36.82F);

        byte[] encode = report.toBytes();
        System.out.println(Hex.encodeHexString(encode));

        TemperatureReport decode = new TemperatureReport();
        decode.fromBytes(encode, 0);
        System.out.println(decode);

        assertEquals(decode.getDeviceId(),report.getDeviceId());
        assertEquals(decode.getTemperature(),report.getTemperature());
    }

    @Test
    void encodeReportProperty() {
        DemoTcpMessage demoTcpMessage = DemoTcpMessage.of(MessageType.REPORT_TEMPERATURE,
                TemperatureReport.of(1000, 36.82F));
        byte[] data = demoTcpMessage.toBytes();
        System.out.println(demoTcpMessage);
        System.out.println(Hex.encodeHexString(data));
        DemoTcpMessage decode = DemoTcpMessage.of(data);

        System.out.println(decode);
        Assertions.assertEquals(demoTcpMessage.getType(),decode.getType());
        Assertions.assertArrayEquals(demoTcpMessage.getData().toBytes(),decode.getData().toBytes());
    }

}