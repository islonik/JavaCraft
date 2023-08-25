package my.javacraft.soap2rest.utils.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Aspect
@Configuration
public class ExecutionTimeAspect {

    @Around("@annotation(my.javacraft.soap2rest.utils.interceptor.ExecutionTime)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Long startedTime = System.currentTimeMillis();

        Object returnObject = point.proceed();

        Long endedTime = System.currentTimeMillis();

        String logOutput =
                """
                \n===============================================================
                Method Signature: %s
                Method Execution: %s ms
                """.formatted(point.getSignature(), endedTime - startedTime);

        log.info(logOutput);

        return returnObject;
    }

}
