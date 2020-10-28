package org.jetlinks.demo.protocol.tcp.message;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.property.ReadPropertyMessage;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ReadProperty implements TcpPayload {

    private ReadPropertyMessage readPropertyMessage;

    @Override
    public byte[] toBytes() {
        return readPropertyMessage.toString().getBytes();
    }

    @Override
    public void fromBytes(byte[] bytes, int offset) {
        ReadPropertyMessage readPropertyMessage = new ReadPropertyMessage();
        byte[] bytes1 = Arrays.copyOfRange(bytes, offset, bytes.length);
        String s = new String(bytes1, StandardCharsets.UTF_8);
        readPropertyMessage.fromJson(JSON.parseObject(s));
        this.readPropertyMessage = readPropertyMessage;
    }
}
