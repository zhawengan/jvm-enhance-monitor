package com.github.zwg.core.util;

import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/17
 */
public class ExportClassUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExportClassUtil.class);

    public static byte[] loadBytes(Class<?> cls) throws IOException {
        if (cls == null) {
            return null;
        }
        String name = cls.getCanonicalName().replaceAll("\\.", "/") + ".class";
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        BufferedInputStream bis = new BufferedInputStream(is);
        try {
            int length = is.available();
            byte[] bs = new byte[length];
            bis.read(bs);
            return bs;
        } finally {
            bis.close();
        }
    }

    public static void dumpClassIfNecessary(String className, byte[] data) {
        final File dumpClassFile = new File("./jemDump/" + className + ".class");
        final File classPath = new File(dumpClassFile.getParent());

        // 创建类所在的包路径
        if (!classPath.mkdirs()
                && !classPath.exists()) {
            logger.warn("create dump classpath:{} failed.", classPath);
            return;
        }
        // 将类字节码写入文件
        try {
            writeByteArrayToFile(dumpClassFile, data);
            logger.info("dump class file success. className:{},filePath:{}", className,
                    dumpClassFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("dump class:{} to file {} failed.", className, dumpClassFile, e);
        }

    }

    public static void dumpAsmIfNecessary(String className, byte[] data) {
        final File dumpClassFile = new File("./jemDump/" + className + "_Asm.log");
        final File classPath = new File(dumpClassFile.getParent());

        // 创建类所在的包路径
        if (!classPath.mkdirs()
                && !classPath.exists()) {
            logger.warn("create dump classpath:{} failed.", classPath);
            return;
        }
        // 将类字节码写入文件
        try {
            writeByteArrayToFile(dumpClassFile, data);
            logger.info("dump class file success. className:{},filePath:{}", className,
                    dumpClassFile.getAbsolutePath());
        } catch (IOException e) {
            logger.warn("dump class:{} to file {} failed.", className, dumpClassFile, e);
        }

    }

}
