package com.github.zwg.core.manager;

import com.github.zwg.core.manager.SearchMatcher.EqualMatcher;
import com.github.zwg.core.manager.SearchMatcher.RegexMatcher;
import com.github.zwg.core.manager.SearchMatcher.WildcardMatcher;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/6
 */
public class MethodMatcher implements Matcher<JemMethod> {

    private Matcher<String> nameMatcher;
    private Matcher<String> descMatcher;

    public MethodMatcher(JemMethod jemMethod) {
        this(null, jemMethod);
    }

    public MethodMatcher(MatchStrategy strategy, JemMethod jemMethod) {
        strategy = strategy == null ? MatchStrategy.WILDCARD : strategy;
        switch (strategy) {
            case REGEX:
                this.nameMatcher = new RegexMatcher(jemMethod.getName());
                this.descMatcher = new RegexMatcher(jemMethod.getDesc());
                break;
            case EQUALS:
                this.nameMatcher = new EqualMatcher(jemMethod.getName());
                this.descMatcher = new EqualMatcher(jemMethod.getDesc());
                break;
            default:
                this.nameMatcher = new WildcardMatcher(jemMethod.getName());
                this.descMatcher = new WildcardMatcher(jemMethod.getDesc());
                break;
        }
    }

    @Override
    public boolean match(JemMethod target) {
        return this.nameMatcher.match(target.getName())
                && this.descMatcher.match(target.getDesc());
    }
}
