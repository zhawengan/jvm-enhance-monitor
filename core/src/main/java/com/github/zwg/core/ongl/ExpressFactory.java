package com.github.zwg.core.ongl;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/9
 */
public class ExpressFactory {

    private static final ThreadLocal<Express> expressRef = ThreadLocal
            .withInitial(() -> new OgnlExpress());

    public static Express newExpress(Object object) {
        return expressRef.get().reset().bind(object);
    }

}
