package org.jetlinks.demo.protocol;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.core.message.CommonDeviceMessage;
import org.jetlinks.core.message.DeviceOfflineMessage;
import org.jetlinks.core.message.DeviceOnlineMessage;
import org.jetlinks.core.message.Message;
import org.jetlinks.core.message.event.EventMessage;
import org.jetlinks.core.message.function.FunctionInvokeMessageReply;
import org.jetlinks.core.message.property.ReadPropertyMessageReply;
import org.jetlinks.core.message.property.ReportPropertyMessage;
import org.jetlinks.core.message.property.WritePropertyMessageReply;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class DemoTopicMessageCodec {

    protected Message doEncode(String topic, JSONObject payload) {
        Message message = null;
        if (topic.startsWith("/fire_alarm")) {
            message = handleFireAlarm(topic, payload);
        } else if (topic.startsWith("/fault_alarm")) {
            message = handleFaultAlarm(topic, payload);
        } else if (topic.startsWith("/dev_msg")) {
            message = handleDeviceMessage(topic, payload);
        } else if (topic.startsWith("/device_online_status")) {
            message = handleDeviceOnlineStatus(topic, payload);
        } else if (topic.startsWith("/read-property")) {
            message = handleReadPropertyReply(payload);
        } else if (topic.startsWith("/report-property")) { //定时上报属性?
            message = handleReportProperty(payload);
        } else if (topic.startsWith("/write-property")) {
            message = handleWritePropertyReply(payload);
        } else if (topic.startsWith("/invoke-function")) {
            message = handleFunctionInvokeReply(payload);
        }
        log.info("handle demo message:{}:{}", topic, payload);
        return message;
    }

    private FunctionInvokeMessageReply handleFunctionInvokeReply(JSONObject json) {
        FunctionInvokeMessageReply reply = new FunctionInvokeMessageReply();
        reply.setFunctionId(json.getString("functionId"));
        reply.setMessageId(json.getString("messageId"));
        reply.setDeviceId(json.getString("deviceId"));
        reply.setOutput(json.get("output"));
        reply.setCode(json.getString("code"));
        reply.setTimestamp(json.getLong("timestamp"));
        reply.setSuccess(json.getBoolean("success"));
        return reply;
    }

    private ReportPropertyMessage handleReportProperty(JSONObject json) {
        ReportPropertyMessage reply = new ReportPropertyMessage();
        reply.setProperties(json.getJSONObject("properties"));
        reply.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());
        reply.setDeviceId(json.getString("deviceId"));
        reply.setTimestamp(json.getLong("timestamp"));
        reply.setSuccess(json.getBoolean("success"));
        return reply;
    }

    private ReadPropertyMessageReply handleReadPropertyReply(JSONObject json) {
        ReadPropertyMessageReply reply = new ReadPropertyMessageReply();
        reply.setProperties(json.getJSONObject("properties"));
        reply.setMessageId(json.getString("messageId"));
        reply.setTimestamp(json.getLong("timestamp"));
        reply.setDeviceId(json.getString("deviceId"));
        reply.setSuccess(json.getBoolean("success"));
        return reply;
    }

    private WritePropertyMessageReply handleWritePropertyReply(JSONObject json) {
        WritePropertyMessageReply reply = new WritePropertyMessageReply();
        reply.setProperties(json.getJSONObject("properties"));
        reply.setMessageId(json.getString("messageId"));
        reply.setTimestamp(json.getLong("timestamp"));
        reply.setDeviceId(json.getString("deviceId"));
        reply.setSuccess(json.getBoolean("success"));
        return reply;
    }

    /*

     {
     "devid": "863703032301165", // 设备编号 "pid": "TBS-110", // 设备型号
     "pname": "TBS-110", // 设备型号名称 "cid": 34, // 单位 ID
     "aid": 1, // 区域 ID
     "a_name": "未来科技城", // 区域名称 "bid": 2, // 建筑 ID
     "b_name": "C2 栋", // 建筑名称
     "lid": 5, // 位置 ID
     "l_name": "4-5-201", // 位置名称
     "time": "2018-01-04 16:28:50", // 消息时间
     "alarm_type": 1, // 报警类型
     "alarm_type_name": "火灾报警", // 报警描述
     "event_id": 32, // 事件 ID
     "event_count": 1, // 该事件消息次数
     "device_type": 1, // 设备的产品类型(1:烟感、2:温感、3:可燃气体、4:手报、5:声光 报、6:网关)
     "comm_type": 2, // 设备的通信方式(1:LoRaWAN、2:NB-IoT)
     "first_alarm_time":"2018-01-04 16:28:50",
     "last_alarm_time":"2018-01-04 16:28:50",
     "lng":22.22,
     "lat":23.23
     }
     */
    private EventMessage handleFireAlarm(String topic, JSONObject json) {
        // String[] topics = topic.split("[/]");
        EventMessage eventMessage = new EventMessage();

        eventMessage.setDeviceId(json.getString("deviceId"));
        eventMessage.setEvent("fire_alarm");
        eventMessage.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());

        eventMessage.setData(new HashMap<>(json));
        eventMessage.setSuccess(true);
        return eventMessage;
    }

    private EventMessage handleFaultAlarm(String topic, JSONObject json) {
        // String[] topics = topic.split("[/]");
        EventMessage eventMessage = new EventMessage();

        eventMessage.setDeviceId(json.getString("deviceId"));
        eventMessage.setEvent("fault_alarm");
        eventMessage.setMessageId(IDGenerator.SNOW_FLAKE_STRING.generate());
        eventMessage.setData(new HashMap<>(json));
        eventMessage.setSuccess(true);
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
        deviceMessage.setDeviceId(json.getString("dno"));

        return deviceMessage;
    }

}
