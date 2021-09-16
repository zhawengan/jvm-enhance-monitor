package com.github.zwg.core.ongl;

import com.github.zwg.core.execption.ExpressException;
import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/9
 */
public class OgnlExpress implements Express {

    private Object bindObject;

    private final OgnlContext context = new OgnlContext();

    @Override
    public Object get(String express) throws ExpressException {
        try {
            context.setMemberAccess(new DefaultMemberAccess(true));
            return Ognl.getValue(express, context, bindObject);
        } catch (Exception ex) {
            throw new ExpressException(express, ex);
        }
    }

    @Override
    public boolean is(String express) {
        try {
            Object ret = get(express);
            return ret instanceof Boolean && (Boolean) ret;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public Express bind(Object object) {
        this.bindObject = object;
        return this;
    }

    @Override
    public Express bind(String name, Object value) {
        context.put(name, value);
        return this;
    }

    @Override
    public Express reset() {
        context.clear();
        return this;
    }
}
