package com.reliaquest.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
        retryFor = {HttpClientErrorException.TooManyRequests.class},
        maxAttempts = 4,
        backoff = @Backoff(delay = 20000, multiplier = 1.5, maxDelay = 60000))
public @interface RetryableApiCall {}
