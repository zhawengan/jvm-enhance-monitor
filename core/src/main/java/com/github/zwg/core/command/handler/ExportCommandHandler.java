package com.github.zwg.core.command.handler;

import static com.github.zwg.core.util.ExportClassUtil.dumpClassIfNecessary;
import static com.github.zwg.core.util.ExportClassUtil.loadBytes;

import com.github.zwg.core.annotation.Arg;
import com.github.zwg.core.annotation.Cmd;
import com.github.zwg.core.command.CommandHandler;
import com.github.zwg.core.command.ParamConstant;
import com.github.zwg.core.manager.MatchStrategy;
import com.github.zwg.core.manager.ReflectClassManager;
import com.github.zwg.core.manager.SearchMatcher;
import com.github.zwg.core.netty.MessageUtil;
import com.github.zwg.core.session.Session;
import java.lang.instrument.Instrumentation;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/17
 */
@Cmd(name = ParamConstant.COMMAND_EXPORT, description = "export class file", help = "export -c *className -f location")
public class ExportCommandHandler implements CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(ExportCommandHandler.class);

    @Arg(name = ParamConstant.CLASS_KEY, description = "find class expression")
    private String classPattern;

    @Arg(name = ParamConstant.REG_KEY, required = false, defaultValue = "WILDCARD", description = "expression matching rules: wildcard, regular, equal")
    private String strategy;


    @Override
    public void execute(Session session, Instrumentation inst) {
        SearchMatcher searchMatcher = new SearchMatcher(MatchStrategy.valueOf(strategy),
                classPattern);
        //1、查询匹配的类
        Collection<Class<?>> classes = ReflectClassManager.getInstance().searchClass(searchMatcher);
        //2、导出class文件
        if (classes != null && classes.size() > 0) {
            for (Class<?> clazz : classes) {
                try {
                    byte[] bytes = loadBytes(clazz);
                    dumpClassIfNecessary(clazz.getSimpleName(), bytes);
                } catch (Exception ex) {
                    logger.warn("export class failed. className:{}", clazz.getName());
                }
            }
        }
        session.sendMessage(MessageUtil.buildPrompt());
    }

}
