package com.zte.mcore.ioc;

import java.util.regex.Pattern;

/**
 * AOP拦截规则
 * 
 * @author PanJun
 * 
 */
public class AdviceRule {

    private final String clsNameRule;
    private final Pattern pattern;
    private final Advice advice;

    public AdviceRule(Advice advice, String clsNameRule) {
        if (advice == null) {
            throw new NullPointerException("The adviceClazz:Advice parameter of AdviceRule contructor is null!");
        }
        if (clsNameRule == null) {
            throw new NullPointerException("The regex parameter of AdviceRule contructor is null!");
        }
        this.advice = advice;
        this.clsNameRule = clsNameRule;
        this.pattern = createPattern(clsNameRule);
    }

    private Pattern createPattern(String regex) {
        StringBuilder sbReg = new StringBuilder("^");
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (c == '.' || c == '^' || c == '$' || c == '(' || c == ')' || c == '[' || c == ']') {
                sbReg.append('\\');
            } else if (c == '*') {
                sbReg.append('.');
            }
            sbReg.append(c);

        }

        sbReg.append("$");
        return Pattern.compile(sbReg.toString());
    }

    /**
     * AOP拦截器对象
     * 
     * @return
     */
    public Advice getAdvice() {
        return advice;
    }

    /**
     * AOP拦截器作用目标类名满足的规则.规制仅支持*作为通配符
     * 
     * @return
     */
    public String getClsNameRule() {
        return clsNameRule;
    }

    /**
     * 类名是否满足此规则
     * 
     * @return
     */
    public boolean matchesRule(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return pattern.matcher(clazz.getName()).find();
    }

}
