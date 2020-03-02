package org.jetlinks.demo.protocol.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
@Getter
public enum TcpStatus {
    SUCCESS((byte) 0),
    ILLEGAL_ARGUMENTS((byte) 40),
    UN_AUTHORIZED((byte) 41),
    INTERNAL_SERVER_ERROR((byte) 50),
    UNKNOWN((byte) -1),
    ;

    private byte status;

    public static Optional<TcpStatus> of(byte value) {
        for (TcpStatus tcpStatus : TcpStatus.values()) {
            if (tcpStatus.getStatus() == value) {
                return Optional.of(tcpStatus);
            }
        }
        return Optional.empty();
    }
}
