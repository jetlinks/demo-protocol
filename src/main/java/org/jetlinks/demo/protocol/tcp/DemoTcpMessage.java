package org.jetlinks.demo.protocol.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.utils.BytesUtils;

import java.util.Arrays;

/**
 * demo tcp 报文协议格式
 * <p>
 * 第0字节为消息类型
 * 第1-4字节为消息体长度
 * 第5-n为消息体
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class DemoTcpMessage {

    private MessageType type;

    private TcpPayload data;

    public static DemoTcpMessage of(byte[] payload) {
        MessageType type = MessageType.of(payload).orElseThrow(IllegalArgumentException::new);
        return DemoTcpMessage.of(type, type.read(payload, 5));
    }

    public ByteBuf toByteBuf(){
        return Unpooled.wrappedBuffer(toBytes());
    }

    public byte[] toBytes() {
        byte[] header = new byte[5];
        header[0] = (byte) type.ordinal();

        byte[] body = type.toBytes(data);
        int bodyLength = body.length;

        BytesUtils.intToBe(header, bodyLength, 1);

        if (bodyLength == 0) {
            return header;
        }
        byte[] data = Arrays.copyOf(header, bodyLength + 5);
        System.arraycopy(body, 0, data, 5, bodyLength);

        return data;
    }

    @Override
    public String toString() {
        return "TcpMessage{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
