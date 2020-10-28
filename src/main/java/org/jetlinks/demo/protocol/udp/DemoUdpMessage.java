package org.jetlinks.demo.protocol.udp;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.message.codec.EncodedMessage;
import org.jetlinks.demo.protocol.tcp.MessageType;
import org.jetlinks.demo.protocol.tcp.TcpPayload;

/**
 * @author wangzheng
 * @see
 * @since 1.0
 */

public interface DemoUdpMessage extends EncodedMessage {

    String getType();

    String getDeviceId();


}
