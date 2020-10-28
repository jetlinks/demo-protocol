package org.jetlinks.demo.protocol.tcp.message;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.property.WritePropertyMessage;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class WriteProperty implements TcpPayload {
    private WritePropertyMessage writePropertyMessage;

    @Override
    public byte[] toBytes() {
        return writePropertyMessage.toString().getBytes();
    }

    @Override
    public void fromBytes(byte[] bytes, int offset) {
        WritePropertyMessage writePropertyMessage = new WritePropertyMessage();
        byte[] bytes1 = Arrays.copyOfRange(bytes, offset, bytes.length);
        String s = new String(bytes1, StandardCharsets.UTF_8);
        writePropertyMessage.fromJson(JSON.parseObject(s));
        this.writePropertyMessage = writePropertyMessage;
    }
}
