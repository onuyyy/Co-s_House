package com.bird.cos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락의 고유 키 (예: "point:lock:user:{userId}")
     */
    String key();

    /**
     * 락 획득 대기 시간 (밀리초 단위)
     */
    long waitTime() default 5L;

    /**
     * 락 유지 시간 (밀리초 단위)
     */
    long leaseTime() default 3L;
}
