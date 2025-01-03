package com.flyingpig.jdbc.transaction;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

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
            transactionManager.begin();
            Object result = pjp.proceed();
            transactionManager.commit();
            return result;
        } catch (Throwable e) {
            transactionManager.rollback();
            throw e;
        }
    }
}
