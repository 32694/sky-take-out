package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect
{

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill) ")
    public void autoFillPointCut()
    {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint  joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段填充");
        //获取当前被拦截的方法
        MethodSignature signature=(MethodSignature) joinPoint.getSignature();
       AutoFill autoFill= signature.getMethod().getAnnotation(AutoFill.class);
       OperationType value = autoFill.value();

       //获取当前被拦截的方法的参数
       Object[] args = joinPoint.getArgs();
       if (args==null||args.length==0){
           return;
       }
       Object object = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = Thread.currentThread().getId();

       if (value== OperationType.INSERT){
           //为插入操作的字段赋值
           //setCreateTime
           //setUpdateTime
           //setCreateUser
           //setUpdateUser
           Method setUpdateTime =object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
          Method setCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
         Method setUpdateUser =  object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
         Method setCreateUser =  object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
         setUpdateTime.invoke(object,now);
         setCreateTime.invoke(object,now);
         setUpdateUser.invoke(object,currentId);
         setCreateUser.invoke(object,currentId);
       }else if (value==OperationType.UPDATE){
           //为更新操作的字段赋值
           //setUpdateTime
           //setUpdateUser
           Method setUpdateTime =object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
           Method setUpdateUser =  object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
           setUpdateTime.invoke(object,now);
           setUpdateUser.invoke(object,currentId);
       }
    }
}
