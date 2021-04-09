package com.liuzw.redisson;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 文件名  ParamParser
 *
 * @author liuzw 2021/4/9 22:15
 */
public class ParamParser {

    /**
     * 用于SpEL表达式解析.
     */
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();

    /**
     * 用于获取方法参数定义名字.
     */
    private static final DefaultParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 解析spEL表达式
     */
    public static String parser(String lockName, Method method, Object[] args) {
        //获取方法形参名数组
        String[] paramNames = NAME_DISCOVERER.getParameterNames(method);
        if (paramNames != null && paramNames.length > 0) {
            Expression expression = SPEL_EXPRESSION_PARSER.parseExpression(lockName);
            // spring的表达式上下文对象
            EvaluationContext context = new StandardEvaluationContext();
            // 给上下文赋值
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            return Objects.requireNonNull(expression.getValue(context)).toString();
        }
        return lockName;
    }
}
