package com.github.zwg.core;

import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class JemLauncher {

    public JemLauncher(String[] args) throws Exception {
        Configuration configuration = parseConfig(args);
        attachAgent(configuration);
    }

    public static void main(String[] args) {
        try {
            new JemLauncher(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("start jem failed.");
            System.exit(-1);
        }
    }

    private void attachAgent(Configuration conf) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> vmdClass = classLoader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
        Class<?> vmClass = classLoader.loadClass("com.sun.tools.attach.VirtualMachine");
        Object attachVmdObj = null;
        List<?> jvmList = (List<?>) vmClass.getMethod("list", (Class<?>[]) null)
                .invoke(null);
        for (Object obj : jvmList) {
            Object jvmId = vmdClass.getMethod("id", (Class<?>[]) null).invoke(obj, (Object[]) null);
            if (jvmId.equals(Integer.toString(conf.getJavaPid()))) {
                attachVmdObj = obj;
            }
        }

        Object vmObj = null;
        try {
            if (null == attachVmdObj) {
                vmObj = vmClass.getMethod("attach", String.class)
                        .invoke(null, "" + conf.getJavaPid());
            } else {
                vmObj = vmClass.getMethod("attach", vmdClass).invoke(null, attachVmdObj);
            }
            System.out.println(vmObj.getClass());
            vmClass.getMethod("loadAgent", String.class, String.class)
                    .invoke(vmObj, conf.getJemAgent(), conf.getJemCore() + ";" + conf.toString());
        } finally {
            if (null != vmObj) {
                vmClass.getMethod("detach", (Class<?>[]) null).invoke(vmObj, (Object[]) null);
            }
        }
    }

    private Configuration parseConfig(String[] args) {
        final OptionParser parser = new OptionParser();
        parser.accepts("pid").withRequiredArg().ofType(int.class).required();
        parser.accepts("target").withOptionalArg().ofType(String.class);
        parser.accepts("multi").withOptionalArg().ofType(int.class);
        parser.accepts("core").withOptionalArg().ofType(String.class);
        parser.accepts("agent").withOptionalArg().ofType(String.class);

        final OptionSet os = parser.parse(args);
        final Configuration config = new Configuration();

        if (os.has("target")) {
            final String[] strSplit = ((String) os.valueOf("target")).split(":");
            config.setTargetIp(strSplit[0]);
            config.setTargetPort(Integer.valueOf(strSplit[1]));
        }

        config.setJavaPid((Integer) os.valueOf("pid"));
        config.setJemAgent((String) os.valueOf("agent"));
        config.setJemCore((String) os.valueOf("core"));

        return config;
    }

}
