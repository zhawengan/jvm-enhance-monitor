package com.github.zwg.core.asm;

import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.Matcher;
import com.github.zwg.core.util.ExportClassUtil;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class EnhanceTransformer implements ClassFileTransformer {

    private final Logger logger = LoggerFactory.getLogger(EnhanceTransformer.class);

    private final String sessionId;
    private final boolean isTracing;
    private final Map<Class<?>, Matcher<JemMethod>> target;

    public EnhanceTransformer(String sessionId, boolean isTracing,
            Map<Class<?>, Matcher<JemMethod>> target) {
        this.sessionId = sessionId;
        this.isTracing = isTracing;
        this.target = target;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        logger.debug("prepare to transform. className:{}", className);
        //过滤掉非目标类
        if (target.get(classBeingRedefined) == null) {
            logger.warn("class is not enhance target. class:{}", classBeingRedefined);
            return null;
        }
        logger.warn("class is enhance target. class:{}", classBeingRedefined);
//        byte[] byteCodes = EnhanceClassManager.getInstance().get(classBeingRedefined);
//        if (byteCodes == null) {
//            logger.info("get cached bytecode null, use classFileBuffer. className:{}", className);
//            byteCodes = classfileBuffer;
//        }
        logger.info("get cached bytecode not null, use classFileBuffer. className:{}", className);
        ClassReader cr = new ClassReader(classfileBuffer);
        Matcher<JemMethod> methodMatcher = target.get(classBeingRedefined);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(new AdviceClassVisitor(sessionId, isTracing, className, methodMatcher, cw),
                ClassReader.EXPAND_FRAMES);
        byte[] classBytes = cw.toByteArray();
        logger.info("class:{} enhance success.", className);
//        if (classBytes != null) {
            ExportClassUtil.dumpClassIfNecessary(className, classBytes);
//        } else {
//            logger.info("ClassWrite create a empty file. className:{}",className);
//        }
        //EnhanceClassManager.getInstance().put(classBeingRedefined, classBytes);
        return classBytes;
    }
}
