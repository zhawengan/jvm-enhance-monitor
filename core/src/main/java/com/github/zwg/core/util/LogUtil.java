package com.github.zwg.core.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/8/31
 */
public class LogUtil {

    private static final Logger logger;

    static {

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        try {

            configurator.doConfigure(LogUtil.class.getResourceAsStream("resources/logback.xml"));
        } catch (JoranException e) {
            throw new RuntimeException("load logback config failed, you need restart", e);
        } finally {
            logger = LoggerFactory.getLogger("jvm-enhance-monitor");
        }

    }

    public static Logger getLogger() {
        return logger;
    }

}
