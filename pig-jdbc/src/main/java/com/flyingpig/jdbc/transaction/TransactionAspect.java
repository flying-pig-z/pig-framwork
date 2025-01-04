package com.flyingpig.jdbc.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// 事务切面
@Aspect
public class TransactionAspect {

    private TransactionManager transactionManager;

    public TransactionAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Around("@annotation(com.flyingpig.jdbc.transaction.Transactional)")
    public Object handleTransaction(ProceedingJoinPoint pjp) throws Throwable {
        try {
            System.out.println("开始事务");
            transactionManager.begin();
            Object result = pjp.proceed();
            System.out.println("提交事务");
            transactionManager.commit();
            return result;
        } catch (Throwable e) {
            System.out.println("事务回滚");
            transactionManager.rollback();
            throw e;
        }
    }
}