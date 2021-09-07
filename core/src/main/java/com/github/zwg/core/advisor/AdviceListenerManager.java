package com.github.zwg.core.advisor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public class AdviceListenerManager {

    private Map<String, AdviceListener> adviceMap = new ConcurrentHashMap<>();

    private static AdviceListenerManager instance;

    private AdviceListenerManager() {

    }

    public void reg(String sessionId, AdviceListener adviceListener) {
        adviceMap.put(sessionId, adviceListener);
    }

    public void unReg(String sessionId,AdviceListener adviceListener){
        adviceMap.remove(sessionId);
    }

    public static AdviceListenerManager getInstance() {
        if (instance == null) {
            synchronized (AdviceListenerManager.class) {
                if (instance == null) {
                    instance = new AdviceListenerManager();
                }
            }
        }
        return instance;
    }

}
