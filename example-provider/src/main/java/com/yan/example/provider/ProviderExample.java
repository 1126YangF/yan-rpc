package com.yan.example.provider;

import com.yan.example.common.service.UserService;
import com.yan.rpc.RpcApplication;
import com.yan.rpc.bootstrap.ProviderBootstrap;
import com.yan.rpc.config.RegistryConfig;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.model.ServiceMetaInfo;
import com.yan.rpc.model.ServiceRegisterInfo;
import com.yan.rpc.registry.LocalRegistry;
import com.yan.rpc.registry.Registry;
import com.yan.rpc.registry.RegistryFactory;
import com.yan.rpc.server.HttpServer;
import com.yan.rpc.server.VertxHttpServer;
import com.yan.rpc.server.tcp.VertxTcpServer;

import java.util.ArrayList;
import java.util.List;


/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo serviceRegisterInfo =
                new ServiceRegisterInfo(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}
