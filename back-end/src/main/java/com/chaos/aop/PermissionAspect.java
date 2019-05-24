package com.chaos.aop;

import com.chaos.annotation.Role;

import com.chaos.config.ConfigValue;
import com.chaos.vo.User;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect {

    @Pointcut("execution(public * com.chaos.controller.api.*.*(..)) && @annotation(com.chaos.annotation.Role)")
    public void rolePointcut() {
    }

    @Before("rolePointcut()")
    public void roleAdvice(JoinPoint joinPoint) throws Exception {
        //1.获取header中的token
        ServletRequestAttributes servletRequestAttributes=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest httpServletRequest=servletRequestAttributes.getRequest();
        HttpServletResponse response = servletRequestAttributes.getResponse();

        String token=httpServletRequest.getHeader("token");
        if(StringUtils.isBlank(token)){
            throw new RuntimeException("token is null");
        }

        //2.根据token获取已登录的用户信息
        User loginUser = ConfigValue.tokenMap.get(token);
        if(loginUser==null){
            throw new RuntimeException("wrong token ,no user be found");
        }


        //3.获取方法所需权限
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        Class<?> targetClass = joinPoint.getTarget().getClass();
        Method targetMethod=null;
        try {
            targetMethod=targetClass.getDeclaredMethod(methodSignature.getName(),method.getParameterTypes());
        } catch (NoSuchMethodException e) {
           throw e;
        }
        Role role = targetMethod.getAnnotation(Role.class);

        //4.校验权限
        if(loginUser.getRole()>role.value()){
            //用户权限小于所需权限 即用户值大于方法值 0代表权限最大
            throw new RuntimeException("permission denied:"+methodSignature.getName());
        }

        //5.设置当前用户
        RequestContextHolder.getRequestAttributes().setAttribute("currentUser",loginUser,RequestAttributes.SCOPE_REQUEST);

    }
}
