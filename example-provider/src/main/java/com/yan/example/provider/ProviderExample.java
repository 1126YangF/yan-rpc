package com.yan.example.provider;

import com.yan.example.common.service.UserService;
import com.yan.rpc.RpcApplication;
import com.yan.rpc.config.RegistryConfig;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.model.ServiceMetaInfo;
import com.yan.rpc.registry.LocalRegistry;
import com.yan.rpc.registry.Registry;
import com.yan.rpc.registry.RegistryFactory;
import com.yan.rpc.server.HttpServer;
import com.yan.rpc.server.VertxHttpServer;


/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
