package com.github.zwg.core.command.handler;

import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
@Cmd(name = ParamConstant.COMMAND_JVM, description = "view the status of related data in the JVM", help = "jvm")
public class JvmCommandHandler implements CommandHandler {

    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    private final Collection<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory
            .getGarbageCollectorMXBeans();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory
            .getOperatingSystemMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();


    @Override
    public void execute(Session session, Instrumentation inst) {
        Map<String, Object> info = new HashMap<>();
        info.put("RUNTIME", getRuntime());
        info.put("CLASS-LOADING", getClassLoading());
        if (!garbageCollectorMXBeans.isEmpty()) {
            info.put("GARBAGE-COLLECTORS", getGarbageCollectors());
        }
        info.put("MEMORY", getMemory());
        info.put("OPERATING-SYSTEM", getOperatingSystem());
        info.put("THREAD", getThreads());
        session.sendCompleteMessage(MessageUtil.buildResponse(info));
    }

    private Map<String, Object> getRuntime() {
        Map<String, Object> data = new HashMap<>();
        data.put("MACHINE-NAME", runtimeMXBean.getName());
        data.put("JVM-START-TIME", runtimeMXBean.getStartTime());
        data.put("MANAGEMENT-SPEC-VERSION", runtimeMXBean.getManagementSpecVersion());
        data.put("SPEC-NAME", runtimeMXBean.getSpecName());
        data.put("SPEC-VENDOR", runtimeMXBean.getSpecVendor());
        data.put("SPEC-VERSION", runtimeMXBean.getSpecVersion());
        data.put("VM-NAME", runtimeMXBean.getVmName());
        data.put("VM-VENDOR", runtimeMXBean.getVmVendor());
        data.put("VM-VERSION", runtimeMXBean.getVmVersion());
        data.put("INPUT-ARGUMENTS", runtimeMXBean.getInputArguments());
        data.put("LIBRARY-PATH", runtimeMXBean.getLibraryPath());
        return data;
    }

    private Map<String, Object> getClassLoading() {
        Map<String, Object> data = new HashMap<>();
        data.put("LOADED-CLASS-COUNT", classLoadingMXBean.getLoadedClassCount());
        data.put("TOTAL-LOADED-CLASS-COUNT", classLoadingMXBean.getTotalLoadedClassCount());
        data.put("UNLOADED-CLASS-COUNT", classLoadingMXBean.getUnloadedClassCount());
        data.put("IS-VERBOSE", classLoadingMXBean.isVerbose());
        return data;
    }

    private Map<String, Object> getGarbageCollectors() {
        Map<String, Object> data = new HashMap<>();
        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            data.put(garbageCollectorMXBean.getName() + " [count/time]",
                    garbageCollectorMXBean.getCollectionCount() + "/" + garbageCollectorMXBean
                            .getCollectionTime() + "(ms)");
        }
        return data;
    }


    private Map<String, Object> getMemory() {
        Map<String, Object> data = new HashMap<>();
        data.put("HEAP-MEMORY-USAGE [committed/init/max/used]",
                memoryMXBean.getHeapMemoryUsage().getCommitted()
                        + "/" + memoryMXBean.getHeapMemoryUsage().getInit()
                        + "/" + memoryMXBean.getHeapMemoryUsage().getMax()
                        + "/" + memoryMXBean.getHeapMemoryUsage().getUsed());
        data.put("NO-HEAP-MEMORY-USAGE [committed/init/max/used]",
                memoryMXBean.getNonHeapMemoryUsage().getCommitted()
                        + "/" + memoryMXBean.getNonHeapMemoryUsage().getInit()
                        + "/" + memoryMXBean.getNonHeapMemoryUsage().getMax()
                        + "/" + memoryMXBean.getNonHeapMemoryUsage().getUsed()
        );
        data.put("PENDING-FINALIZE-COUNT", memoryMXBean.getObjectPendingFinalizationCount());
        return data;
    }


    private Map<String, Object> getOperatingSystem() {
        Map<String, Object> data = new HashMap<>();
        data.put("OS", operatingSystemMXBean.getName());
        data.put("ARCH", operatingSystemMXBean.getArch());
        data.put("PROCESSORS-COUNT", operatingSystemMXBean.getAvailableProcessors());
        data.put("LOAD-AVERAGE", operatingSystemMXBean.getSystemLoadAverage());
        data.put("VERSION", operatingSystemMXBean.getVersion());
        return data;
    }

    private Map<String, Object> getThreads() {
        Map<String, Object> data = new HashMap<>();
        data.put("COUNT", threadMXBean.getThreadCount());
        data.put("DAEMON-COUNT", threadMXBean.getDaemonThreadCount());
        data.put("LIVE-COUNT", threadMXBean.getPeakThreadCount());
        data.put("STARTED-COUNT", threadMXBean.getTotalStartedThreadCount());
        return data;
    }


}
