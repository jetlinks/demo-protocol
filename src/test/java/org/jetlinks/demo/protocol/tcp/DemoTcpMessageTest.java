package org.jetlinks.demo.protocol.tcp;

import org.apache.commons.codec.binary.Hex;
import org.jetlinks.demo.protocol.tcp.message.AuthRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DemoTcpMessageTest {

    @Test
    void test() {
        DemoTcpMessage message = DemoTcpMessage.of(MessageType.AUTH_REQ, AuthRequest.of(1000, "admin"));

        byte[] data = message.toBytes();
        System.out.println(Hex.encodeHexString(data));

        DemoTcpMessage decode = DemoTcpMessage.of(data);

        System.out.println(decode);

        Assertions.assertEquals(message.getType(),decode.getType());
        Assertions.assertArrayEquals(message.getData().toBytes(),decode.getData().toBytes());




    }

}