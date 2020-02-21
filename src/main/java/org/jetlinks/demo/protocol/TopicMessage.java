package org.jetlinks.demo.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetlinks.core.message.DeviceMessage;

@Getter
@Setter
@AllArgsConstructor
public class TopicMessage {

    private String topic;

    private Object message;
}
