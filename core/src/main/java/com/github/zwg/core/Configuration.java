package com.github.zwg.core;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/16
 */
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
                targetPort = Integer.valueOf(params[1]);
                javaPid = Integer.valueOf(params[2]);
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

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public int getJavaPid() {
        return javaPid;
    }

    public void setJavaPid(int javaPid) {
        this.javaPid = javaPid;
    }

    public String getJemCore() {
        return jemCore;
    }

    public void setJemCore(String jemCore) {
        this.jemCore = jemCore;
    }

    public String getJemAgent() {
        return jemAgent;
    }

    public void setJemAgent(String jemAgent) {
        this.jemAgent = jemAgent;
    }
}
