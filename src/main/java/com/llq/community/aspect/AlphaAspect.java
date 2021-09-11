package com.llq.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    //切入点，表示哪些方法可以得到增强
    @Pointcut("execution(* com.llq.community.service.*.*(..))")
    public void pointcut() {

    }
    //在该方法执行之前执行
    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }
    //在返回值之后执行
    @AfterReturning("pointcut()")
    public void afterRetuning() {
        System.out.println("afterRetuning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //在目前调用的方法之前执行（优先级高于before）
        System.out.println("around before");
        //调用目前访问的方法
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }

}
