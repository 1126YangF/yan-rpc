package com.yan.rpc.fault.tolerant;

import com.yan.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 静默处理异常 - 容错策略
 *
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //记录日志
        log.info("静默处理异常", e);
        //返回空对象
        return new RpcResponse();
    }
}
