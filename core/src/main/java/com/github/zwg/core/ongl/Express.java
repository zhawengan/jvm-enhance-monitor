package com.github.zwg.core.ongl;

import com.github.zwg.core.execption.ExpressException;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/9
 */
public interface Express {

    Object get(String express) throws ExpressException;

    boolean is(String express);

    Express bind(Object object);

    Express bind(String name,Object value);

    Express reset();
}
