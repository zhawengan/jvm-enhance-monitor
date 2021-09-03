package com.github.zwg.core.manager;

/**
 * @author zwg
 * @version 1.0
 * @date 2021/9/3
 */
public class SearchMatcher implements Matcher<String> {

    private final Matcher<String> matcher;

    public SearchMatcher(String pattern) {
        this(null, pattern);
    }

    public SearchMatcher(MatchStrategy strategy, String pattern) {
        strategy = strategy == null ? MatchStrategy.WILDCARD : strategy;
        switch (strategy) {
            case REGEX:
                this.matcher = new RegexMatcher(pattern);
                break;
            case EQUALS:
                this.matcher = new EqualMatcher(pattern);
                break;
            default:
                this.matcher = new WildcardMatcher(pattern);
                break;
        }
    }

    @Override
    public boolean match(String target) {
        return target != null && this.matcher.match(target);
    }


    /**
     * 正则匹配
     */
    class RegexMatcher implements Matcher<String> {

        private final String pattern;

        public RegexMatcher(String pattern) {

            this.pattern = pattern;
        }

        @Override
        public boolean match(String target) {
            return target.matches(pattern);
        }
    }

    /**
     * 字符串相等
     */
    class EqualMatcher implements Matcher<String> {

        private final String pattern;

        public EqualMatcher(String pattern) {

            this.pattern = pattern;
        }

        @Override
        public boolean match(String target) {
            return target.equalsIgnoreCase(pattern);
        }
    }

    /**
     * 通配符匹配
     */
    class WildcardMatcher implements Matcher<String> {

        private final String pattern;

        public WildcardMatcher(String pattern) {

            this.pattern = pattern;
        }

        @Override
        public boolean match(String target) {
            return match(target, pattern, 0, 0);
        }

        private boolean match(String target, String pattern, int stringStartNdx,
                int patternStartNdx) {
            int pNdx = patternStartNdx;
            int sNdx = stringStartNdx;
            int pLen = pattern.length();
            if (pLen == 1) {
                if (pattern.charAt(0) == '*') {     // speed-up
                    return true;
                }
            }
            int sLen = target.length();
            boolean nextIsNotWildcard = false;

            while (true) {
                // check if end of string and/or pattern occurred
                if ((sNdx >= sLen)) {   // end of string still may have pending '*' callback pattern
                    while ((pNdx < pLen) && (pattern.charAt(pNdx) == '*')) {
                        pNdx++;
                    }
                    return pNdx >= pLen;
                }
                if (pNdx >= pLen) {         // end of pattern, but not end of the string
                    return false;
                }
                char p = pattern.charAt(pNdx);    // pattern char

                // perform logic
                if (!nextIsNotWildcard) {

                    if (p == '\\') {
                        pNdx++;
                        nextIsNotWildcard = true;
                        continue;
                    }
                    if (p == '?') {
                        sNdx++;
                        pNdx++;
                        continue;
                    }
                    if (p == '*') {
                        char pnext = 0;           // next pattern char
                        if (pNdx + 1 < pLen) {
                            pnext = pattern.charAt(pNdx + 1);
                        }
                        if (pnext == '*') {         // double '*' have the same effect as one '*'
                            pNdx++;
                            continue;
                        }
                        int i;
                        pNdx++;
                        // find recursively if there is any substring from the end of the
                        // line that matches the rest of the pattern !!!
                        for (i = target.length(); i >= sNdx; i--) {
                            if (match(target, pattern, i, pNdx)) {
                                return true;
                            }
                        }
                        return false;
                    }
                } else {
                    nextIsNotWildcard = false;
                }
                // check if pattern char and string char are equals
                if (p != target.charAt(sNdx)) {
                    return false;
                }
                // everything matches for now, continue
                sNdx++;
                pNdx++;
            }
        }
    }


}
