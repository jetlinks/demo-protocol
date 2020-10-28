package org.jetlinks.demo.protocol.tcp.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.utils.BytesUtils;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

import java.util.Arrays;

/**
 * 前8为设备ID,低位字节在前.
 * 后面为Key.
 *
 * @author zhouhao
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest implements TcpPayload {

    private long deviceId;

    private byte[] key;

    public static AuthRequest of(long deviceId, String key) {
        return new AuthRequest(deviceId, key.getBytes());
    }

    public static AuthRequest of(byte[] data) {
        AuthRequest message = new AuthRequest();
        message.fromBytes(data, 0);
        return message;
    }

    @Override
    public byte[] toBytes() {
        byte[] keyBytes = key == null ? new byte[0] : key;
        byte[] idBytes = BytesUtils.longToBe(deviceId);
        byte[] data = Arrays.copyOf(idBytes, keyBytes.length + idBytes.length);
        System.arraycopy(keyBytes, 0, data, idBytes.length, keyBytes.length);
        return data;
    }

    @Override
    public void fromBytes(byte[] bytes, int offset) {
        this.deviceId = BytesUtils.beToLong(bytes, offset, 8);
        this.key = Arrays.copyOfRange(bytes, offset + 8, bytes.length);
    }

    @Override
    public String toString() {
        return "{" +
                "deviceId=" + deviceId +
                ", key=" + (new String(key)) +
                '}';
    }
}
