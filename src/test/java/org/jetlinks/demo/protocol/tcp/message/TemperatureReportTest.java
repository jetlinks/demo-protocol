package org.jetlinks.demo.protocol.tcp.message;

import org.apache.commons.codec.binary.Hex;
import org.hswebframework.web.id.IDGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.util.IdGenerator;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureReportTest {


    @Test
    void test() {
        TemperatureReport report = TemperatureReport.of(IDGenerator.SNOW_FLAKE.generate(), 36.82F);

        byte[] encode = report.toBytes();
        System.out.println(Hex.encodeHexString(encode));

        TemperatureReport decode = new TemperatureReport();
        decode.fromBytes(encode, 0);
        System.out.println(decode);

        assertEquals(decode.getDeviceId(),report.getDeviceId());
        assertEquals(decode.getTemperature(),report.getTemperature());
    }

}