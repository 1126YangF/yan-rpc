package com.yan.rpc.fault.tolerant;

import cn.hutool.core.collection.CollUtil;
import com.yan.rpc.RpcApplication;
import com.yan.rpc.fault.retry.RetryStrategy;
import com.yan.rpc.fault.retry.RetryStrategyFactory;
import com.yan.rpc.model.RpcRequest;
import com.yan.rpc.model.RpcResponse;
import com.yan.rpc.registry.Registry;
import com.yan.rpc.registry.RegistryFactory;
import lombok.extern.slf4j.Slf4j;
import com.yan.rpc.model.ServiceMetaInfo;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.server.tcp.VertxTcpClient;
import com.yan.rpc.loadbalancer.LoadBalancer;
import com.yan.rpc.loadbalancer.LoadBalancerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 转移到其他服务节点 - 容错策略
 *
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //获取其它节点并调用
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("serviceMetaInfoList");
        ServiceMetaInfo selectedServiceMetaInfo = (ServiceMetaInfo) context.get("selectedServiceMetaInfo");
        //获取配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        //移除失败节点
        removeFailNode(selectedServiceMetaInfo, serviceMetaInfoList, rpcConfig);

        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("methodName", rpcRequest.getMethodName());

        while (!serviceMetaInfoList.isEmpty()) {
            ServiceMetaInfo currentServiceMetaInfo = loadBalancer.select(requestParamMap, serviceMetaInfoList);
            System.out.println("获取节点：" + currentServiceMetaInfo);
            try {
                //发送tcp请求
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                RpcResponse rpcResponse = retryStrategy.doRetry(() -> VertxTcpClient.doRequest(rpcRequest, currentServiceMetaInfo));
                return rpcResponse;
            } catch (Exception exception) {
                //移除失败节点
                removeFailNode(currentServiceMetaInfo, serviceMetaInfoList, rpcConfig);
            }
        }
        //调用失败
        throw new RuntimeException(e);
    }

    /**
     * 移除失败节点，进行下线
     *
     * @param serviceMetaInfoList
     * @param rpcConfig
     */
    private void removeFailNode(ServiceMetaInfo currentServiceMetaInfo, List<ServiceMetaInfo> serviceMetaInfoList, RpcConfig rpcConfig) {
        if (CollUtil.isNotEmpty(serviceMetaInfoList)) {
            Iterator<ServiceMetaInfo> iterator = serviceMetaInfoList.iterator();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            while (iterator.hasNext()) {
                ServiceMetaInfo next = iterator.next();
                if (currentServiceMetaInfo.getServiceNodeKey().equals(next.getServiceNodeKey())) {
                    iterator.remove();
                    //节点下线
                    log.error("节点下线：" + next.getServiceNodeKey());
                    try {
                        registry.unRegister(next);
                        break;
                    } catch (ExecutionException | InterruptedException e) {
                        //节点下线失败
                        log.error("节点下线失败：" + next.getServiceNodeKey());
                    }
                }
            }
        }
    }
}