package com.bird.cos.aop;

import com.bird.cos.annotation.DistributedLock;
import com.bird.cos.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 트랜잭션보다 먼저 실행
@Slf4j
@RequiredArgsConstructor
public class DistributedLockAop {

  private final RedissonClient redissonClient;
  // SpEL 파싱기 -> #userId 같은 표현식 평가에 사용
  private final ExpressionParser expressionParser = new SpelExpressionParser();
  // 메서드 매개변수 이름 가져오는 도구 -> userId를 알아야 #userId 해석 가능
  private final ParameterNameDiscoverer parameterNameDiscoverer =
          new DefaultParameterNameDiscoverer();

  @Around("@annotation(com.bird.cos.annotation.DistributedLock)")
  public Object executeWithDistributedLock(ProceedingJoinPoint joinPoint) throws Throwable {
    // 메서드 정보 추출
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    // 실제 메서드 객체 가져오기
    Method method = signature.getMethod();
    // 어노테이션 읽어서 key, waitTime, leaseTime 가져오기
    DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    // 락 키 생성 (예: "point:lock:user:{userId}")
    String lockKey = resolveKey(joinPoint, method, distributedLock.key());

    // 해당 key로 락 객체 가져오기
    RLock lock = redissonClient.getLock(lockKey);
    // 락 획득 여부 변수
    boolean locked = false;

    try {
      // 락 획득 시도
      locked = lock.tryLock(
              distributedLock.waitTime(),
              distributedLock.leaseTime(),
              TimeUnit.MILLISECONDS
      );

      // 락 획득 실패 시 예외 발생
      if (!locked) {
        throw BusinessException.pointOperationFailed("사용", "분산락 획득에 실패했습니다. lockKey: " + lockKey);
      }

      log.debug("분산락 획득 - key: {}", lockKey);
      // 실제 메서드 실행
      return joinPoint.proceed();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw BusinessException.pointOperationFailed("사용", "분산락 대기 중 인터럽트가 발생했습니다. lockKey: " + lockKey);
    } finally {
      if (locked && lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.debug("분산락 해제 - key: {}", lockKey);
      }
    }
  }

  private String resolveKey(ProceedingJoinPoint joinPoint, Method method, String keyExpression) {
    MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
            joinPoint.getTarget(),
            method,
            joinPoint.getArgs(),
            parameterNameDiscoverer
    );

    // SpEL 표현식을 평가하여 락 키를 생성
    return expressionParser.parseExpression(keyExpression).getValue(context, String.class);
  }

}
