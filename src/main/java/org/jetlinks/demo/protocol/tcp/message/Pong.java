package org.jetlinks.demo.protocol.tcp.message;

import org.jetlinks.demo.protocol.tcp.TcpPayload;

public class Pong implements TcpPayload {
    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public void fromBytes(byte[] bytes, int offset) {

    }
}
