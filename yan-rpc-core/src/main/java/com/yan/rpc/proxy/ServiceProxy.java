package com.yan.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yan.rpc.fault.retry.RetryStrategy;
import com.yan.rpc.fault.retry.RetryStrategyFactory;
import com.yan.rpc.fault.tolerant.TolerantStrategy;
import com.yan.rpc.fault.tolerant.TolerantStrategyFactory;
import com.yan.rpc.loadbalancer.LoadBalancer;
import com.yan.rpc.loadbalancer.LoadBalancerFactory;
import com.yan.rpc.protocol.*;
import com.yan.rpc.serializer.Serializer;
import com.yan.rpc.RpcApplication;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.constant.RpcConstant;
import com.yan.rpc.model.RpcRequest;
import com.yan.rpc.model.RpcResponse;
import com.yan.rpc.model.ServiceMetaInfo;
import com.yan.rpc.registry.Registry;
import com.yan.rpc.registry.RegistryFactory;
import com.yan.rpc.serializer.SerializerFactory;
import com.yan.rpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 服务代理（JDK 动态代理）
 *
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //系统内置的动态加载指定接口的实现类
//        ServiceLoader<Serializer> load = ServiceLoader.load(Serializer.class);
        System.out.println("method============ " + method);
        System.out.println("args============ " + Arrays.toString(args));
        // 读取配置文件
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 指定序列化器
        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
        System.out.println(serializer);

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        System.out.println("请求serviceName============ " + serviceName);
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 从注册中心获取服务提供者请求地址
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            //获取服务注册信息
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            // 将调用方法名（请求路径）作为负载均衡参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

            //用于测试连接失败
            selectedServiceMetaInfo.setServiceHost(selectedServiceMetaInfo.getServiceHost() + "123");

            // 发送 TCP 请求  使用重试机制
            RpcResponse rpcResponse;
            try {
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() ->
                        VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
                );
            } catch (Exception e) {
                // 容错机制
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
                Map<String, Object> requestTolerantParamMap = new HashMap<>();
                requestTolerantParamMap.put("rpcRequest",rpcRequest);
                requestTolerantParamMap.put("selectedServiceMetaInfo",selectedServiceMetaInfo);
                requestTolerantParamMap.put("serviceMetaInfoList",serviceMetaInfoList);
                rpcResponse = tolerantStrategy.doTolerant(requestTolerantParamMap, e);
            }
            return rpcResponse.getData();

            // 发送http请求
            // 序列化
//            byte[] bodyBytes = serializer.serialize(rpcRequest);
//            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
//                    .body(bodyBytes)
//                    .execute()) {
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return rpcResponse.getData();
//            }

        } catch (Exception e) {
            throw new RuntimeException("调用失败");
        }
    }
}
