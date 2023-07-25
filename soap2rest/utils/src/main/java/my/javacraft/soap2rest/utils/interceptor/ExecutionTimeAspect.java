package my.javacraft.soap2rest.utils.interceptor;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class ExecutionTimeAspect {

    @Pointcut("@annotation(ExecutionTime)")
    public void timePointcut(ProceedingJoinPoint point) throws Throwable {
        Long startedTime = System.currentTimeMillis();

        point.proceed();

        Long endedTime = System.currentTimeMillis();
        log.info(point.getSignature() + " method shows: " + (endedTime - startedTime) + " ms");
    }

}
