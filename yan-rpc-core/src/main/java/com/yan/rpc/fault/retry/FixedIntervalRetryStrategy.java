package com.yan.rpc.fault.retry;

import com.github.rholder.retry.*;
import com.yan.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔 - 重试策略
 *
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws ExecutionException
     * @throws RetryException
     */
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        // 重试器
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 设置重试条件：如果抛出Exception类的异常，则重试
                .retryIfExceptionOfType(Exception.class)
                // 设置等待策略：每次重试之间固定等待3秒
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                // 设置停止策略：最多重试3次后停止
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 重试监听器：每次重试时执行
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        // 重试时执行
        return retryer.call(callable);
    }

}
