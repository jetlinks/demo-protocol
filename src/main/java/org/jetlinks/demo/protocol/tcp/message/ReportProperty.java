package org.jetlinks.demo.protocol.tcp.message;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ReportProperty implements TcpPayload {
    private ReportPropertyMessage reportPropertyMessage;

    @Override
    public byte[] toBytes() {
        return reportPropertyMessage.toString().getBytes();
    }

    @Override
    public void fromBytes(byte[] bytes, int offset) {
        ReportPropertyMessage reportPropertyMessage = new ReportPropertyMessage();
        byte[] bytes1 = Arrays.copyOfRange(bytes, offset, bytes.length);
        String s = new String(bytes1, StandardCharsets.UTF_8);
        reportPropertyMessage.fromJson(JSON.parseObject(s));
        this.reportPropertyMessage = reportPropertyMessage;
    }
}
