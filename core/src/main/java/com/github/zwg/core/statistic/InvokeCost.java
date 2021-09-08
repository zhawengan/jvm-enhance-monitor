package com.github.zwg.core.statistic;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/8
 */
public class InvokeCost {

    private final ThreadLocal<Long> timestampRef = new ThreadLocal<>();

    public long begin() {
        long timestamp = System.currentTimeMillis();
        timestampRef.set(timestamp);
        return timestamp;
    }

    public long cost() {
        return System.currentTimeMillis() - timestampRef.get();
    }
}
