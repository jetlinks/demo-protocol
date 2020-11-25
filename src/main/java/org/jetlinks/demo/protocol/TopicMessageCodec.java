package org.jetlinks.demo.protocol;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.core.message.*;
import org.jetlinks.core.message.codec.SimpleMqttMessage;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.firmware.*;
import org.jetlinks.core.message.function.FunctionInvokeMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessageReply;
import org.jetlinks.core.message.property.*;

import java.util.HashMap;


@Slf4j
public class TopicMessageCodec {

    protected DeviceMessage doDecode(String deviceId, String topic, JSONObject payload) {
        DeviceMessage message = null;
        if (topic.startsWith("/fire_alarm")) {
            message = handleFireAlarm(topic, payload);
        } else if (topic.startsWith("/fault_alarm")) {
            message = handleFaultAlarm(topic, payload);
        } else if (topic.startsWith("/register")) {
            message = handleRegister(payload);
        } else if (topic.startsWith("/unregister")) {
            message = handleUnRegister(payload);
        } else if (topic.startsWith("/dev_msg")) {
            message = handleDeviceMessage(topic, payload);
        } else if (topic.startsWith("/device_online_status")) {
            message = handleDeviceOnlineStatus(topic, payload);
        } else if (topic.startsWith("/read-property")) {
            message = handleReadPropertyReply(payload);
        } else if (topic.startsWith("/report-property")) { //定时上报属性
            message = handleReportProperty(payload);
        } else if (topic.startsWith("/write-property")) {
            message = handleWritePropertyReply(payload);
        } else if (topic.startsWith("/invoke-function")) {
            message = handleFunctionInvokeReply(payload);
        } else if (topic.startsWith("/open-door")) {
            message = handleOpenTheDoor(topic, payload);
        } else if (topic.startsWith("/children")) {
            ChildDeviceMessage childDeviceMessage = new ChildDeviceMessage();
            childDeviceMessage.setDeviceId(deviceId);
            DeviceMessage children = doDecode(deviceId, topic.substring(9), payload);
            childDeviceMessage.setChildDeviceMessage(children);
            childDeviceMessage.setChildDeviceId(children.getDeviceId());
            message = childDeviceMessage;
        }
        // 固件相关1.0.3版本后增加,注意: 专业版中才有固件相关业务功能
        else if (topic.startsWith("/firmware/report")) {//上报固件信息
            message = payload.toJavaObject(ReportFirmwareMessage.class);
        } else if (topic.startsWith("/firmware/progress")) { //上报升级进度
            message = payload.toJavaObject(UpgradeFirmwareProgressMessage.class);
        } else if (topic.startsWith("/firmware/pull")) { //拉取固件信息
            message = payload.toJavaObject(RequestFirmwareMessage.class);
        } else if (topic.startsWith("/tags")) { //更新tags
            message = payload.toJavaObject(UpdateTagMessage.class);
        }

        log.info("handle demo message:{}:{}", topic, payload);
        return message;
    }


    protected TopicMessage doEncode(DeviceMessage message) {
        if (message instanceof ReadPropertyMessage) {
            String topic = "/read-property";
            JSONObject data = new JSONObject();
            data.put("messageId", message.getMessageId());
            data.put("deviceId", message.getDeviceId());
            data.put("properties", ((ReadPropertyMessage) message).getProperties());
            return new TopicMessage(topic, data);
        } else if (message instanceof WritePropertyMessage) {
            String topic = "/write-property";
            JSONObject data = new JSONObject();
            data.put("messageId", message.getMessageId());
            data.put("deviceId", message.getDeviceId());
            data.put("properties", ((WritePropertyMessage) message).getProperties());
            return new TopicMessage(topic, data);
        } else if (message instanceof FunctionInvokeMessage) {
            String topic = "/invoke-function";
            FunctionInvokeMessage invokeMessage = ((FunctionInvokeMessage) message);
            JSONObject data = new JSONObject();
            data.put("messageId", message.getMessageId());
            data.put("deviceId", message.getDeviceId());
            data.put("function", invokeMessage.getFunctionId());
            data.put("args", invokeMessage.getInputs());
            return new TopicMessage(topic, data);
        } else if (message instanceof ChildDeviceMessage) {
            TopicMessage msg = doEncode((DeviceMessage) ((ChildDeviceMessage) message).getChildDeviceMessage());
            if (msg == null) {
                return null;
            }
            String topic = "/children" + msg.getTopic();
            return new TopicMessage(topic, msg.getMessage());
        }

        //平台推送固件更新,设备无需回复此消息.
        else if (
            message instanceof UpgradeFirmwareMessage ||
                message instanceof RequestFirmwareMessageReply
        ) {
            String topic = "/firmware/push";
            return new TopicMessage(topic, JSON.toJSON(message));
        }
        return null;
    }

    private FunctionInvokeMessageReply handleFunctionInvokeReply(JSONObject json) {
        return json.toJavaObject(FunctionInvokeMessageReply.class);
    }

    private DeviceRegisterMessage handleRegister(JSONObject json) {
        DeviceRegisterMessage reply = new DeviceRegisterMessage();
        reply.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());
        reply.setDeviceId(json.getString("deviceId"));
        reply.setTimestamp(System.currentTimeMillis());
        reply.setHeaders(json.getJSONObject("headers"));
        return reply;
    }

    private DeviceUnRegisterMessage handleUnRegister(JSONObject json) {
        DeviceUnRegisterMessage reply = new DeviceUnRegisterMessage();
        reply.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());
        reply.setDeviceId(json.getString("deviceId"));
        reply.setTimestamp(System.currentTimeMillis());
        return reply;
    }

    private ReportPropertyMessage handleReportProperty(JSONObject json) {
        ReportPropertyMessage msg = ReportPropertyMessage.create();
        msg.fromJson(json);
        return msg;
    }

    private ReadPropertyMessageReply handleReadPropertyReply(JSONObject json) {
        return json.toJavaObject(ReadPropertyMessageReply.class);
    }

    private WritePropertyMessageReply handleWritePropertyReply(JSONObject json) {
        return json.toJavaObject(WritePropertyMessageReply.class);
    }

    private EventMessage handleFireAlarm(String topic, JSONObject json) {
        EventMessage eventMessage = new EventMessage();

        eventMessage.setDeviceId(json.getString("deviceId"));
        eventMessage.setEvent("fire_alarm");
        eventMessage.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());

        eventMessage.setData(new HashMap<>(json));
        return eventMessage;
    }

    private EventMessage handleOpenTheDoor(String topic, JSONObject json) {
        EventMessage eventMessage = new EventMessage();

        eventMessage.setDeviceId(json.getString("deviceId"));
        eventMessage.setEvent("open-door");
        eventMessage.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());

        eventMessage.setData(new HashMap<>(json));
        return eventMessage;
    }

    private EventMessage handleFaultAlarm(String topic, JSONObject json) {
        // String[] topics = topic.split("[/]");
        EventMessage eventMessage = new EventMessage();

        eventMessage.setDeviceId(json.getString("deviceId"));
        eventMessage.setEvent("fault_alarm");
        eventMessage.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());
        eventMessage.setData(new HashMap<>(json));
        return eventMessage;
    }

    private EventMessage handleDeviceMessage(String topic, JSONObject json) {
        EventMessage eventMessage = new EventMessage();

        eventMessage.setDeviceId(json.getString("deviceId"));
        eventMessage.setEvent("dev_msg");
        eventMessage.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());
        eventMessage.setData(new HashMap<>(json));
        return eventMessage;
    }

    private CommonDeviceMessage handleDeviceOnlineStatus(String topic, JSONObject json) {
        CommonDeviceMessage deviceMessage;

        if ("1".equals(json.getString("status"))) {
            deviceMessage = new DeviceOnlineMessage();
        } else {
            deviceMessage = new DeviceOfflineMessage();
        }
        deviceMessage.setDeviceId(json.getString("deviceId"));

        return deviceMessage;
    }

}
