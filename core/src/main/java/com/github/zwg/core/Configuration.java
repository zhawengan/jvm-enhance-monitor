package com.github.zwg.core;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/16
 */
@Data
public class Configuration {

    private String targetIp;
    private int targetPort = 9191;
    private int javaPid;
    private String jemCore;
    private String jemAgent;

    public Configuration() {

    }

    public Configuration(String args) {
        if (!StringUtils.isBlank(args)) {
            String[] params = args.split("@");
            if (params.length == 5) {
                targetIp = params[0];
                targetPort = Integer.parseInt(params[1]);
                javaPid = Integer.parseInt(params[2]);
                jemCore = params[3];
                jemAgent = params[4];
            }
        }
    }

    @Override
    public String toString() {
        return targetIp + "@" +
                targetPort + "@" +
                javaPid + "@" +
                jemCore + "@" +
                jemAgent;
    }
}
