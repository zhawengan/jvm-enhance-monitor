package com.github.zwg.core.netty;

import com.github.zwg.core.util.JacksonObjectFormat;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class MessageUtil {

    private static final JacksonObjectFormat objectFormat = new JacksonObjectFormat();

    public static Message wrap(String sessionId, MessageTypeEnum type, Map<String, String> headers,
            Object data) {
        Message message = new Message();
        message.setMagicNumber(Constants.MAGIC_NUMBER);
        message.setMajorVersion(Constants.MAJOR_VERSION);
        message.setMinorVersion(Constants.MINOR_VERSION);
        message.setModifyVersion(Constants.MODIFY_VERSION);
        message.setSessionId(sessionId);
        message.setMessageType(type);
        if (headers != null) {
            message.getHeaders().putAll(headers);
        }
        if (data != null) {
            if (data instanceof String) {
                message.setBody(data.toString());
            } else {
                message.setBody(objectFormat.toJson(data));
            }
        }
        return message;
    }

}
