package com.route.datasource.config;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import com.route.datasource.annotation.LookupKey;
import com.route.datasource.annotation.RoutingMapper;
import com.route.datasource.model.CommonDTO;
import com.route.datasource.util.ThreadLocalContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Configuration
public class RoutingMapperAspect {
  
  @Around("execution(* com.route.datasource..repository.routing..*Mapper.*(..))")
  public Object aroundTargetMethod(ProceedingJoinPoint thisJoinPoint) {
    MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
    Class<?> mapperInterface = methodSignature.getDeclaringType();
    Method method = methodSignature.getMethod();
    Parameter[] parameters = method.getParameters();
    Object[] args = thisJoinPoint.getArgs();
    
    RoutingMapper routingMapper = mapperInterface.getDeclaredAnnotation(RoutingMapper.class);
    if (routingMapper != null) {
      Integer worldId = findLookupKey(parameters, args);
      log.debug("Routed worldId: {}", worldId);
      ThreadLocalContext.set(worldId);
    }
    
    try {
      return thisJoinPoint.proceed();
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    } finally {
      ThreadLocalContext.remove();
    }
  }
  
  // DB Route를 위한 LookUp키를 @LookupKey를 붙여놓은 파라미터 안에서 찾는다.
  // 여기에서는 월드 번호를 예시로 들기에, 단일 Integer 또는 공통 DTO안의 World를 찾아 보낸다.
  private Integer findLookupKey(Parameter[] parameters, Object[] args) {
    for (int i = 0; i < parameters.length; i++) {
      LookupKey lookupKey = parameters[i].getDeclaredAnnotation(LookupKey.class);
      if (lookupKey != null) {
        if (args[i] instanceof Integer) {
          return Integer.valueOf(args[i].toString());
        } else if (args[i] instanceof CommonDTO) {
          CommonDTO dto = (CommonDTO) args[i];
          return dto.worldId;
        }
        
      }
    }
    throw new RuntimeException("can't find LookupKey");
  }
}