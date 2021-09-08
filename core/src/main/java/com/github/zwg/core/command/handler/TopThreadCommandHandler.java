package com.github.zwg.core.command.handler;

import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.session.Session;
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

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/4
 */
@Cmd(name = ParamConstant.COMMAND_TOP_THREAD, description = "find the top N threads that consume the most resources", help = {
        "top",
        "top -n 10"
})
public class TopThreadCommandHandler implements CommandHandler {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    @Arg(name = ParamConstant.NUMBER_PARAM, required = false, defaultValue = "10", description = "find the top N threads")
    private Integer threadNum;

    @Override
    public void execute(Session session, Instrumentation inst,
            MonitorCallback callback) {
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
        Integer num = Math.min(threadNum, threadInfos.size());
        Collections.sort(threadInfos);
        threadInfos = threadInfos.subList(0, num);
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

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
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
