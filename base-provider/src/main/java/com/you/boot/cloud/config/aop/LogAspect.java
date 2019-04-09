package com.you.boot.cloud.config.aop;

import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shicz
 */
//@Aspect
//@Component
public class LogAspect {
    Logger logger = LoggerFactory.getLogger(LogAspect.class);

    private final String UNKNOWN = "unknown";

    @Pointcut("execution(* com.you.boot.cloud.controller..*(..))")
    public void logaop(){}


    @Around("logaop()")
    public Object method(ProceedingJoinPoint pjp) throws Throwable {
        Map<String,Object> result = new HashMap<String,Object>(16);
        HttpServletRequest request = getHttpServletRequest();
        result.put("ip", getRemoteIp(request));
        result.put("method",request.getMethod());
        result.put("uri",request.getRequestURI());
        Object[] args = pjp.getArgs();
        String params = "";
        if (args.length == 1){
            params = JSONObject.toJSONString(pjp.getArgs()[0]);
        }else if (args.length > 1){
            params = JSONObject.toJSONString(pjp.getArgs());
        }
        result.put("params",params);
        String className = pjp.getSignature().getDeclaringTypeName();
        result.put("className",className);
        String funName = pjp.getSignature().getName();
        result.put("funName",funName);
        Method[] methods = pjp.getSignature().getDeclaringType().getDeclaredMethods();
        Method method = null;
        for (Method m : methods){
            if (m.getName().equals(funName)){
                method = m;
                break;
            }
        }
        long startTime = System.currentTimeMillis();
        if (method == null){
            return  pjp.proceed();
        }else {
            Object object = pjp.proceed();
            long time = (System.currentTimeMillis() - startTime)/1000;
            result.put("time",time);
            logger.debug("log===>>"+JSONObject.toJSONString(result));
            return object;
        }
    }



    private HttpServletRequest getHttpServletRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private String getRemoteIp(HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip))
        {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
