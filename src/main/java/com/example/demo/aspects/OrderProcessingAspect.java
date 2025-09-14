package com.example.demo.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//import java.util.Objects;
//import java.util.logging.Logger;

import org.slf4j.Logger;

import java.util.Arrays;
//import org.slf4j.LoggerFactory;

@Aspect
@Component
public class OrderProcessingAspect {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingAspect.class);

    @Around("execution(* com.example.demo.services.OrderService.placeOrder(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.info("Started processing Order: {}", methodName);
        logger.info("Arguments: {}", Arrays.toString(args));

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - start;
            logger.info("Order processed in {} ms", duration);
            return result;
        }catch(Throwable e){
            logger.error("Exception in processing Order: {}", e.getMessage());
            throw e;
        }
    }
}

