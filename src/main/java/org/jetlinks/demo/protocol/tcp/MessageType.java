package org.jetlinks.demo.protocol.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetlinks.demo.protocol.tcp.message.*;

import java.util.Optional;
import java.util.function.Supplier;

@AllArgsConstructor
@Getter
public enum MessageType {
    AUTH_REQ("认证请求", AuthRequest::new),
    AUTH_RES("认证结果", AuthResponse::new),
    ERROR("错误", ErrorMessage::new),
    PING("Ping", Ping::new), PONG("Pong", Pong::new),
    REPORT_TEMPERATURE("上报温度", TemperatureReport::new),
    //    READ_TEMPERATURE("读取温度"),
//    READ_TEMPERATURE_REPLY("读取温度回复"),
    FIRE_ALARM("火警", FireAlarm::new);
    private String text;

    private Supplier<TcpPayload> payloadSupplier;

    public TcpPayload read(byte[] payload, int offset) {
        TcpPayload tcpPayload = payloadSupplier.get();
        tcpPayload.fromBytes(payload, offset);
        return tcpPayload;
    }

    public byte[] toBytes(TcpPayload data) {
        if (data == null) {
            return new byte[0];
        }
        return data.toBytes();
    }

    public static Optional<MessageType> of(byte[] payload) {
        byte type = payload[0];
        MessageType[] values = values();
        if (type > values.length) {
            return Optional.empty();
        }
        return Optional.of(values()[type]);
    }
}
