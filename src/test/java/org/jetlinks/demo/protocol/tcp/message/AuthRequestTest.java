package org.jetlinks.demo.protocol.tcp.message;

import org.apache.commons.codec.binary.Hex;
import org.hswebframework.web.id.IDGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestTest {


    @Test
    void test() {
        AuthRequest message = AuthRequest.of(1000L, "admin");

        byte[] payload = message.toBytes();
        System.out.println(Hex.encodeHexString(payload));

        AuthRequest decode = AuthRequest.of(payload);

        System.out.println(decode);
        assertEquals(message.getDeviceId(), decode.getDeviceId());
        assertArrayEquals(message.getKey(), decode.getKey());


    }
}