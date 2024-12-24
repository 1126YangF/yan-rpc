package com.yan.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yan.rpc.RpcApplication;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.constant.RpcConstant;
import com.yan.rpc.model.RpcRequest;
import com.yan.rpc.model.RpcResponse;
import com.yan.rpc.model.ServiceMetaInfo;
import com.yan.rpc.registry.Registry;
import com.yan.rpc.registry.RegistryFactory;
import com.yan.rpc.serializer.JdkSerializer;
import com.yan.rpc.serializer.Serializer;
import com.yan.rpc.serializer.SerializerFactory;
import com.yan.rpc.utils.ConfigUtils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;

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

        // 读取配置文件
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 指定序列化器
        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
        System.out.println(serializer);

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        System.out.println("============serviceName: " + serviceName);
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 从注册中心获取服务提供者请求地址
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
