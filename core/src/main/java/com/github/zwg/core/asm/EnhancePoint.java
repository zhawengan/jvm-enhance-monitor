package com.github.zwg.core.asm;

import com.github.zwg.core.manager.JemMethod;
import com.github.zwg.core.manager.Matcher;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/5
 */
public class EnhancePoint {

    private Matcher<String> classMatcher;

    private Matcher<JemMethod> methodMatcher;

    public EnhancePoint(Matcher<String> classMatcher,
            Matcher<JemMethod> methodMatcher) {
        this.classMatcher = classMatcher;
        this.methodMatcher = methodMatcher;
    }

    public Matcher<String> getClassMatcher() {
        return classMatcher;
    }

    public void setClassMatcher(Matcher<String> classMatcher) {
        this.classMatcher = classMatcher;
    }

    public Matcher<JemMethod> getMethodMatcher() {
        return methodMatcher;
    }

    public void setMethodMatcher(Matcher<JemMethod> methodMatcher) {
        this.methodMatcher = methodMatcher;
    }
}
