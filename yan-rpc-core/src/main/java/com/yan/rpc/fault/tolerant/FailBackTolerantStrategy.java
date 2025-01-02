package com.yan.rpc.fault.tolerant;


import com.yan.rpc.model.RpcRequest;
import com.yan.rpc.model.RpcResponse;
import com.yan.rpc.proxy.ServiceProxyFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 降级到其他服务 - 容错策略
 *
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 调用降级节点
        log.error("调用降级节点");
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        String serviceName = rpcRequest.getServiceName();
        try {
            //通过反射调用降级接口
            Class<?> aClass = Class.forName(serviceName);
            Object mockProxy = ServiceProxyFactory.getMockProxy(aClass);
            Method method = mockProxy.getClass()
                    .getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(mockProxy, rpcRequest.getArgs());

            // 返回结果
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setData(result);
            rpcResponse.setDataType(method.getReturnType());
            rpcResponse.setMessage("Fail Back Tolerant Strategy");

            return rpcResponse;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
