package com.forgerock.openidm.aspect;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 *
 * @author Vilo Repan
 */
@Aspect
public abstract class ProfilingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ProfilingAspect.class);

    @Around("methodsToBeProfiled()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        if (logger.isTraceEnabled()) {
            StringBuilder message = new StringBuilder();
            message.append("Entry: ");
            message.append(getClassName(pjp));
            message.append(" ");
            message.append(pjp.getSignature().getName());
            message.append(", args: ");
            message.append(Arrays.toString(pjp.getArgs()));
            logger.trace(message.toString());
        }

        StopWatch sw = new StopWatch(getClass().getSimpleName());
        Object retValue = null;
        try {
            sw.start(pjp.getSignature().getName());
            retValue = pjp.proceed();
            return retValue;
        } finally {
            sw.stop();

            if (logger.isTraceEnabled()) {
                StringBuilder message = new StringBuilder();
                message.append("Exit: ");
                message.append(getClassName(pjp));
                message.append(", time: ");
                message.append(sw.getTotalTimeMillis());
                message.append(", value: ");
                message.append(retValue);

                logger.trace(message.toString());
            }
        }
    }

    private String getClassName(ProceedingJoinPoint pjp) {
        if (pjp.getThis() != null) {
            return pjp.getThis().getClass().getName();
        }

        return null;
    }

    @Pointcut
    public abstract void methodsToBeProfiled();
}
