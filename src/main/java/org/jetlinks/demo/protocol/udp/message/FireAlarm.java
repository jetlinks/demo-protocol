package org.jetlinks.demo.protocol.udp.message;

import io.netty.buffer.ByteBuf;
import lombok.*;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.demo.protocol.tcp.TcpDeviceMessage;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FireAlarm implements UdpPayload {

    //设备ID
    private long deviceId;

    //经度
    private float lnt;

    //纬度
    private float lat;

    //点位
    private int point;

    private String bName;

    @Override
    public String getType() {
        return "fire-alarm";
    }


    @Nonnull
    @Override
    public ByteBuf getPayload() {
        return null;
    }

    public String getDeviceId() {
        return String.valueOf(this.deviceId);
    }
}
