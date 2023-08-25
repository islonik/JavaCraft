# Soap to Rest - Utils

Contains <b>ExecutionTime</b> annotation which could help to evaluate performance of your REST calls to backend services.

Declaration of the public annotation.
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
}
```

Declares how we are going to implement the actual performance evaluation.
```java
@Slf4j
@Aspect
@Configuration
public class ExecutionTimeAspect {

    @Around("@annotation(my.javacraft.soap2rest.utils.interceptor.ExecutionTime)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Long startedTime = System.currentTimeMillis();

        Object returnObject = point.proceed();

        Long endedTime = System.currentTimeMillis();

        log.info(point.getSignature() + " method executes in: " + (endedTime - startedTime) + " ms");

        return returnObject;
    }

}
```