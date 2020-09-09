package org.jetlinks.demo.protocol.udp.message;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ReportProperty implements UdpPayload {
    private ReportPropertyMessage reportPropertyMessage;

    @Override
    public String getDeviceId() {
        return reportPropertyMessage.getDeviceId();
    }

    @Override
    public String getType() {
        return "report-property";
    }

    @Nonnull
    @Override
    public ByteBuf getPayload() {
        return null;
    }
}
