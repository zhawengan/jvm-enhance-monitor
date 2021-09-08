package com.github.zwg.core.command.handler;

import static java.lang.System.arraycopy;

import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.asm.AsmTraceClassVisitor;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.MonitorCallback;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.ReflectClassManager;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.session.Session;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31 View the bytecode of the corresponding class or method
 */
@Cmd(name = ParamConstant.COMMAND_ASM, description = "view the bytecode information of the corresponding class or method",
        help = {
                "asm -c *className",
                "asm -c *className -m *methodName",
                "asm -r WILDCARD -c *className -m *methodName"
        })
public class AsmCommandHandler implements CommandHandler {

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.METHOD_KEY, required = false, defaultValue = "*", description = "find method expression")
    private String methodPattern;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;


    @Override
    public void execute(Session session, Instrumentation inst,
            MonitorCallback callback) {
        SearchMatcher classMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        SearchMatcher methodMatcher = new SearchMatcher(
                StringUtils.isBlank(strategy) ? MatchStrategy.WILDCARD
                        : MatchStrategy.valueOf(strategy),
                methodPattern);
        //2、查询匹配的类
        Collection<Class<?>> classes = ReflectClassManager.getInstance()
                .searchClass(classMatcher);
        //3、获取字节码
        List<AsmClassInfo> classInfos = transformClassInfo(classes, inst);
        Map<String, Object> result = new HashMap<>();
        for (AsmClassInfo classInfo : classInfos) {
            if (classInfo.clazz.isArray()) {
                continue;
            }
            InputStream is = new ByteArrayInputStream(classInfo.byteArray);
            StringWriter sw = new StringWriter();
            try {
                ClassReader cr = new ClassReader(is);
                AsmTraceClassVisitor traceClassVisitor = new AsmTraceClassVisitor(
                        new PrintWriter(sw, true)) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                            String signature, String[] exceptions) {
                        if (methodMatcher.match(name)) {
                            return super
                                    .visitMethod(access, name, descriptor, signature, exceptions);
                        } else {
                            return null;
                        }
                    }
                };
                cr.accept(traceClassVisitor, ClassReader.SKIP_DEBUG);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            result.put(classInfo.clazz.getName(), sw.toString());
        }

        callback.execute(result);

    }

    private List<AsmClassInfo> transformClassInfo(Collection<Class<?>> classes,
            Instrumentation inst) {
        List<AsmClassInfo> classInfos = new ArrayList<>();
        if (classes == null || classes.isEmpty()) {
            return classInfos;
        }
        ClassFileTransformer classFileTransformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            if (classes.contains(classBeingRedefined)) {
                classInfos.add(new AsmClassInfo(classBeingRedefined, loader, classfileBuffer,
                        protectionDomain));
            }
            return null;
        };
        try {
            inst.addTransformer(classFileTransformer);
            int size = classes.size();
            Class<?>[] classArray = new Class[size];
            arraycopy(classes, 0, classArray, 0, size);
            inst.retransformClasses(classArray);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            inst.removeTransformer(classFileTransformer);
        }
        return classInfos;
    }


    public class AsmClassInfo {

        private final Class<?> clazz;
        private final ClassLoader classLoader;
        private final byte[] byteArray;
        private final ProtectionDomain protectionDomain;

        public AsmClassInfo(Class<?> clazz, ClassLoader classLoader, byte[] byteArray,
                ProtectionDomain protectionDomain) {

            this.clazz = clazz;
            this.classLoader = classLoader;
            this.byteArray = byteArray;
            this.protectionDomain = protectionDomain;
        }
    }
}
