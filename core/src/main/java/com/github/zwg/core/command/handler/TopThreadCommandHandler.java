package com.github.zwg.core.command.handler;

import com.github.zwg.core.command.Command;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.session.Session;
import com.github.zwg.core.util.ParamConstant;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/4
 */
public class TopThreadCommandHandler implements CommandHandler {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    @Override
    public String getCommandName() {
        return "top";
    }

    @Override
    public void execute(Session session, Command command, Instrumentation inst,
            MonitorCallback callback) {
        String threadNum = command.getOptions().get(ParamConstant.NUMBER_PARAM);
        long totalCpuTime = threadMXBean.getCurrentThreadCpuTime();
        List<JemThreadInfo> threadInfos = new ArrayList<>();
        ThreadInfo[] threadInfoList = threadMXBean
                .getThreadInfo(threadMXBean.getAllThreadIds(), Integer.MAX_VALUE);
        for (int i = 0; i < threadInfoList.length; i++) {
            ThreadInfo curt = threadInfoList[i];
            long threadCpuTime = threadMXBean.getThreadCpuTime(curt.getThreadId());
            totalCpuTime += threadCpuTime;
            threadInfos.add(new JemThreadInfo(curt.getThreadId(),
                    threadCpuTime, curt.getThreadName(),
                    curt.getThreadState().toString(),
                    Arrays.stream(curt.getStackTrace()).map(StackTraceElement::toString)
                            .collect(Collectors.toList())));
        }
        if (!StringUtils.isBlank(threadNum)) {
            Integer num = Math.min(Integer.parseInt(threadNum), threadInfos.size());
            Collections.sort(threadInfos);
            threadInfos = threadInfos.subList(0, num);
        }
        callback.execute(getThreadInfoData(threadInfos, totalCpuTime));


    }

    private Map<String, Object> getThreadInfoData(Collection<JemThreadInfo> threadInfos,
            long totalTime) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> threadData = new ArrayList<>();
        final DecimalFormat df = new DecimalFormat("00.00");
        for (JemThreadInfo thread : threadInfos) {
            String cpuTimeRateStr =
                    (totalTime > 0 ? df.format(thread.cpuTime * 100d / totalTime) : "00.00") + "%";
            Map<String, Object> data = new HashMap<>();
            data.put("ID", thread.id);
            data.put("CPU%", cpuTimeRateStr);
            data.put("NAME", thread.name);
            data.put("STATE", thread.state);
            data.put("STACK", thread.stack);
            threadData.add(data);
        }
        result.put("THREAD-INFO", threadData);
        return result;
    }

    public class JemThreadInfo implements Comparable<JemThreadInfo> {

        private final long id;
        private final long cpuTime;
        private final String name;
        private final String state;
        private final List<String> stack;

        public JemThreadInfo(long id, long cpuTime, String name, String state, List<String> stack) {
            this.id = id;

            this.cpuTime = cpuTime;
            this.name = name;
            this.state = state;
            this.stack = stack;
        }

        @Override
        public int compareTo(JemThreadInfo o) {
            return Long.valueOf(o.cpuTime).compareTo(cpuTime);
        }
    }
}
