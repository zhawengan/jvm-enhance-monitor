package com.github.zwg.core.command.handler;

import com.github.zwg.core.advisor.AdviceListener;
import com.github.zwg.core.advisor.AdviceListenerManager;
import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.asm.EnhancePoint;
import com.github.zwg.core.asm.Enhancer;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.MethodMatcher;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.statistic.InvokeCost;
import java.lang.instrument.Instrumentation;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Cmd(name = ParamConstant.COMMAND_MONITOR)
public class MonitorCommandHandler implements CommandHandler {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.METHOD_KEY, description = "find method expression")
    private String methodPattern;

    @Arg(name = ParamConstant.METHOD_DESC, required = false, defaultValue = "*", description = "method description expression")
    private String methodDesc;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;

    @Arg(name = ParamConstant.PERIOD, required = false, defaultValue = "120", description = "The cycle of monitor")
    private Integer period;

    @Override
    public void execute(Session session, Instrumentation inst) {
        Enhancer.enhance(inst, session.getSessionId(), false, getPoint());
        AdviceListener adviceListener = getAdviceListener(session);
        adviceListener.start();
        AdviceListenerManager.reg(session.getSessionId(), adviceListener);
    }

    private EnhancePoint getPoint() {
        SearchMatcher classMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        JemMethod jemMethod = new JemMethod(methodPattern, methodDesc);
        MethodMatcher methodMatcher = new MethodMatcher(jemMethod);
        return new EnhancePoint(classMatcher, methodMatcher);
    }

    /**
     * 实现具体的监控数据处理流程
     */
    private AdviceListener getAdviceListener(Session session) {
        return new AdviceListener() {

            private Timer timer;

            /**
             * 考虑到被监控的方法可能存在多线程并发，针对每个线程，需要有单独的数据
             */
            private final InvokeCost invokeCost = new InvokeCost();

            private final AtomicBoolean consumeFlag = new AtomicBoolean(true);

            private final BlockingQueue<MonitorStash> stashData = new LinkedBlockingQueue<>();

            private final ConcurrentHashMap<String, MonitorData> monitorMap
                    = new ConcurrentHashMap<>();

            @Override
            public void start() {
                timer = new Timer("monitor-" + session.getSessionId(), true);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        for (Map.Entry<String, MonitorData> item : monitorMap
                                .entrySet()) {
                            MonitorData data = item.getValue();
                            if (data != null) {
                                String[] keys = item.getKey().split(",");
                                Map<String, Object> result = new HashMap<>();
                                result.put("TIMESTAMP", dateFormat.format(new Date()));
                                result.put("CLASS", keys[0]);
                                result.put("METHOD", keys[1]);
                                result.put("TOTAL", data.total);
                                result.put("SUCCESS", data.success);
                                result.put("FAIL", data.failed);
                                result.put("FAIL-RATE", data.failRate);
                                result.put("AVG-RT(ms)", data.avgRate);
                                result.put("MIN-RT(ms)", data.minCost);
                                result.put("MAX-RT(ms)", data.maxCost);
                                result.put("DELAY-QUEUE", stashData.size());
                                //callback.execute(result);
                                session.sendMessage(MessageUtil.buildResponse(result));
                            }
                        }
                    }
                }, 0, period * 1000);
                Thread consumer = new Thread(() -> {
                    while (consumeFlag.get()) {
                        try {
                            MonitorStash stash = stashData.poll(100, TimeUnit.MILLISECONDS);
                            if (stash != null) {
                                monitorCount(stash);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                consumer.setName("monitor-consume-" + session.getSessionId());
                consumer.start();
            }

            @Override
            public void beforeMethod(ClassLoader classLoader, String className, String methodName,
                    String methodDesc, Object target, Object[] args) {
                invokeCost.begin();
            }

            @Override
            public void afterMethodReturning(ClassLoader classLoader, String className,
                    String methodName, String methodDesc, Object target, Object[] args,
                    Object returnObject) {
                finish(className, methodName, true);
            }

            @Override
            public void afterMethodThrowing(ClassLoader classLoader, String className,
                    String methodName, String methodDesc, Object target, Object[] args,
                    Throwable throwable) {
                finish(className, methodName, false);
            }

            @Override
            public void destroy() {
                consumeFlag.set(false);
                if (timer != null) {
                    timer.cancel();
                }
            }

            /**
             * 采用异步模式，将数据存入缓存，不阻塞业务线程
             */
            private void finish(String className, String methodName, boolean isSuccess) {
                long cost = invokeCost.cost();
                stashData.add(new MonitorStash(className, methodName, isSuccess, cost));
            }

            private double div(double a, double b) {
                if (b == 0) {
                    return 0;
                }
                return a / b;
            }

            private void monitorCount(MonitorStash stash) {
                String key = stash.className + "," + stash.methodName;
                MonitorData data = monitorMap.putIfAbsent(key, new MonitorData());
                data.total = data.total + 1;
                if (stash.isSuccess) {
                    data.success = data.success + 1;
                } else {
                    data.failed = data.failed + 1;
                }
                data.cost = data.cost + stash.cost;
                if (data.minCost == null) {
                    data.minCost = stash.cost;
                } else {
                    data.minCost = Math.min(data.minCost, stash.cost);
                }
                if (data.maxCost == null) {
                    data.maxCost = stash.cost;
                } else {
                    data.maxCost = Math.max(data.maxCost, stash.cost);
                }
                DecimalFormat df = new DecimalFormat("00.00");
                data.failRate = df.format(100.0d * div(data.failed, data.total)) + "%";
                data.avgRate = df.format(div(data.cost, data.total)) + "%";
            }
        };
    }

    public static class MonitorData {

        private int total;
        private int success;
        private int failed;
        private long cost;
        private String failRate;
        private String avgRate;
        private Long maxCost;
        private Long minCost;
    }

    public static class MonitorStash {

        private final String className;
        private final String methodName;
        private final boolean isSuccess;
        private final long cost;

        public MonitorStash(String className, String methodName, boolean isSuccess, long cost) {

            this.className = className;
            this.methodName = methodName;
            this.isSuccess = isSuccess;
            this.cost = cost;
        }
    }
}
