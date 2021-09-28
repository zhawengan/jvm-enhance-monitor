package com.github.zwg.core.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public class GroupMatcher<T> implements Matcher<T> {

    private List<Matcher<T>> matcherList = new ArrayList<>();

    public void add(Matcher<T> matcher) {
        if (matcher != null) {
            matcherList.add(matcher);
        }
    }

    @Override
    public boolean match(T target) {
        for (Matcher<T> matcher : matcherList) {
            if (matcher.match(target)) {
                return true;
            }
        }
        return false;
    }
}
