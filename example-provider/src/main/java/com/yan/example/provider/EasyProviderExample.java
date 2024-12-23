package com.yan.example.provider;

import com.yan.example.common.service.UserService;
import com.yan.rpc.RpcApplication;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.registry.LocalRegistry;
import com.yan.rpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 *
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        //PRC框架初始化
        RpcApplication.getRpcConfig();
        //在本地注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        //启动web服务
        VertxHttpServer vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
