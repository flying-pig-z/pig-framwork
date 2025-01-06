package com.flyingpig.aop.core;

// 切点表达式解析器

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 切点表达式解析器
 * 用于解析和匹配AOP切点表达式
 */
public class PointcutExpressionParser {

    /**
     * 根据表达式匹配方法
     * @param targetMethods 目标方法数组
     * @param expression 切点表达式
     * @return 匹配的方法列表
     */
    public static List<Method> getMatchedMethods(Method[] targetMethods, String expression) {
        List<Method> matchedMethods = new ArrayList<>();
        ExpressionParts parts = parseExpressionParts(expression);

        for (Method method : targetMethods) {
            if (isMethodMatched(method, parts)) {
                matchedMethods.add(method);
            }
        }

        return matchedMethods;
    }

    /**
     * 检查方法是否匹配表达式
     */
    private static boolean isMethodMatched(Method method, ExpressionParts parts) {
        switch (parts.type) {
            case ANNOTATION:
                return matchesAnnotation(method, parts.annotationName);
            case EXECUTION:
                return matchesExecution(method, parts);
            case WITHIN:
                return matchesWithin(method, parts);
            case METHOD:
                return matchesMethod(method, parts.methodName);
            default:
                return false;
        }
    }
    /**
     * 注解匹配方法
     */
    private static boolean matchesAnnotation(Method method, String annotationName) {
        try {
            // 获取目标注解类
            Class<?> annotationClass = Class.forName(annotationName);
            // 检查方法是否有指定注解
            return method.isAnnotationPresent(annotationClass.asSubclass(java.lang.annotation.Annotation.class));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 解析表达式
     */
    private static ExpressionParts parseExpressionParts(String expression) {
        ExpressionParts parts = new ExpressionParts();
        if (expression.startsWith("@annotation(")) {
            // 处理注解表达式
            parts.type = ExpressionType.ANNOTATION;
            parts.content = expression.substring("@annotation(".length(), expression.length() - 1);
            parts.annotationName = parts.content;
        } else if (expression.startsWith("execution(")) {
            parts.type = ExpressionType.EXECUTION;
            parts.content = expression.substring("execution(".length(), expression.length() - 1);
            parseExecutionExpression(parts);
        } else if (expression.startsWith("within(")) {
            parts.type = ExpressionType.WITHIN;
            parts.content = expression.substring("within(".length(), expression.length() - 1);
            parts.packageName = parts.content;
        } else {
            parts.type = ExpressionType.METHOD;
            parts.methodName = expression;
        }

        return parts;
    }

    /**
     * 解析execution表达式
     */
    private static void parseExecutionExpression(ExpressionParts parts) {
        String[] segments = parts.content.trim().split("\\s+");

        // 解析返回类型
        parts.returnType = segments[0];

        // 解析最后一个部分（方法签名）
        String methodPart = segments[segments.length - 1];
        int paramStart = methodPart.indexOf('(');
        int paramEnd = methodPart.indexOf(')');

        // 解析方法名
        parts.methodName = methodPart.substring(0, paramStart);

        // 解析参数
        parts.parameters = methodPart.substring(paramStart + 1, paramEnd);

        // 解析包名（如果存在）
        if (segments.length > 2) {
            parts.packageName = segments[1];
        }
    }

    /**
     * 匹配execution表达式
     */
    private static boolean matchesExecution(Method method, ExpressionParts parts) {
        // 匹配返回类型
        if (!matchesReturnType(method, parts.returnType)) {
            return false;
        }

        // 匹配包名
        if (parts.packageName != null && !matchesPackage(method, parts.packageName)) {
            return false;
        }

        // 匹配方法名
        if (!matchesMethod(method, parts.methodName)) {
            return false;
        }

        // 匹配参数
        return matchesParameters(method, parts.parameters);
    }

    /**
     * 匹配返回类型
     */
    private static boolean matchesReturnType(Method method, String returnTypePattern) {
        if (returnTypePattern.equals("*")) {
            return true;
        }
        return method.getReturnType().getSimpleName().equals(returnTypePattern);
    }

    /**
     * 匹配包名
     */
    private static boolean matchesPackage(Method method, String packagePattern) {
        String actualPackage = method.getDeclaringClass().getPackage().getName();

        // 转换通配符为正则表达式
        String regex = packagePattern
                .replace(".", "\\.")
                .replace("*", "[^.]+")
                .replace("..", ".*");

        return actualPackage.matches(regex);
    }

    /**
     * 匹配within表达式
     */
    private static boolean matchesWithin(Method method, ExpressionParts parts) {
        return matchesPackage(method, parts.packageName);
    }

    /**
     * 匹配方法名
     */
    private static boolean matchesMethod(Method method, String namePattern) {
        String regex = namePattern.replace("*", ".*");
        return method.getName().matches(regex);
    }

    /**
     * 匹配参数
     */
    private static boolean matchesParameters(Method method, String paramsPattern) {
        if (paramsPattern.equals("..")) {
            return true;
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramsPattern.isEmpty()) {
            return paramTypes.length == 0;
        }

        if (paramsPattern.equals("*")) {
            return paramTypes.length == 1;
        }

        String[] patterns = paramsPattern.split(",");
        if (patterns.length != paramTypes.length) {
            return false;
        }

        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i].trim();
            if (!pattern.equals("*") && !pattern.equals(paramTypes[i].getSimpleName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 表达式类型
     */
    private enum ExpressionType {
        EXECUTION,
        WITHIN,
        METHOD,
        ANNOTATION
    }


    /**
     * 表达式解析结果
     */
    private static class ExpressionParts {
        ExpressionType type;
        String content;
        String returnType;
        String packageName;
        String methodName;
        String parameters;
        String annotationName;
    }
}