package com.github.zwg.core.netty;

import com.github.zwg.core.util.JacksonUtil;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/1
 */
public class MessageUtil {

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
                message.setBody(JacksonUtil.toJsonPretty(data));
            }
        }
        return message;
    }

    public static Message buildRegister(String sessionId, Object data) {
        return wrap(sessionId, MessageTypeEnum.REGISTER, null, data);
    }

    public static Message buildRequest(String sessionId, Object data) {
        return wrap(sessionId, MessageTypeEnum.REQUEST, null, data);
    }

    public static Message buildResponse(Object data) {
        return wrap(null, MessageTypeEnum.RESPONSE, null, data);
    }

    public static Message buildAllResponse(String sessionId, Object data) {
        return wrap(sessionId, MessageTypeEnum.RESPONSE_ALL, null, data);
    }

    public static Message buildPrompt() {
        return wrap("", MessageTypeEnum.PROMPT, null, Constants.PROMPT);
    }

    public static Message buildInterrupt(String sessionId){
        return wrap(sessionId, MessageTypeEnum.INTERRUPT, null, Constants.PROMPT);
    }

}
