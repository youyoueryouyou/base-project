package com.you.boot.cloud.config.aop;

import com.alibaba.fastjson.JSONObject;
import com.you.base.BaseCondition;
import com.you.base.BaseModel;
import com.you.base.BasePojo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shicz
 */
@Aspect
@Component
public class MapperAspect {
    @Pointcut("execution(* com.you.boot.cloud.mapper..*(..))")
    public void logaop() {
    }

    @Around("logaop()")
    public Object method(ProceedingJoinPoint pjp) throws Throwable {
        String name = JSONObject.parseObject(JSONObject.toJSONString(pjp.getSignature())).getString("name");
        Object object = pjp.proceed();
        if ("selectByPrimaryKey".equals(name)) {
            if (object != null && object instanceof BasePojo) {
                BasePojo pojo = (BasePojo) object;
                if (pjp.getArgs().length > 0) {
                    Class cl = pjp.getArgs()[0].getClass();
                    Object obj = cl.newInstance();
                    if (obj instanceof BaseModel) {
                        BaseModel bm = (BaseModel) obj;
                        return pojo.getPojo(bm);
                    }
                }
            }
        } else if ("selectByCondition".equals(name)) {
            if (object != null && object instanceof List) {
                List list = (ArrayList) object;
                if (list.size() > 0) {
                    if (pjp.getArgs().length > 0) {
                        Object ob = pjp.getArgs()[0];
                        if (ob instanceof BaseCondition) {
                            Class cl = ((BaseCondition) ob).getModel().getClass();
                            Object obj = cl.newInstance();
                            if (obj instanceof BaseModel) {
                                List result = new ArrayList();
                                for (Object rst : list) {
                                    if (rst instanceof BasePojo) {
                                        BasePojo pojo = (BasePojo) rst;
                                        BaseModel bm = (BaseModel) cl.newInstance();
                                        result.add(pojo.getPojo(bm));
                                    }
                                }
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return object;
    }
}
