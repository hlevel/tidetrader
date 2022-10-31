package tide.trader.bot.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tide.trader.bot.common.exception.RateLimitException;
import tide.trader.bot.common.mvc.RateLimit;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogOperationAspect {

    //Link access limit per minute
    private RateLimit limit = new RateLimit(1000*60, 35);


    @Pointcut("execution(public * tide.trader.bot.controllers.*.*(..))")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        long endTime = 0l;
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        try {
            if(limit.isLimit()) {
                throw new RateLimitException("Your access exceeds the limit");
            }

            //执行方法
            Object result = point.proceed();
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            //执行时长(毫秒)
            endTime = System.currentTimeMillis() - beginTime;
            log.info("[{}]'{}',parameter:{}, execute {} ms", request.getMethod(), request.getRequestURL().toString(), Arrays.toString(point.getArgs()), endTime);
        }
    }

    @AfterThrowing(value = "logPointCut()", throwing = "throwable")
    public void doAfterThrowing(Throwable throwable) {
        log.error("system error", throwable.getMessage());
    }

}
