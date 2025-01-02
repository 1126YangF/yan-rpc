package com.yan.yanrpcspringbootstarter.bootstrap;


import com.yan.rpc.RpcApplication;
import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.proxy.ServiceProxyFactory;
import com.yan.yanrpcspringbootstarter.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * Rpc 服务消费者启动
 *
 */
@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注入服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取当前bean的类类型
        Class<?> beanClass = bean.getClass();
        //  遍历bean类中的所有属性
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            // 判断属性是否带有 RpcReference 注解
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 获取注解中指定的接口类，如果没指定，则使用属性的类型作为接口类
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                //获取指定的重试策略与容错策略进行重新设置
                String retryStrategy = rpcReference.retryStrategy();
                String tolerantStrategy = rpcReference.tolerantStrategy();
                RpcConfig rpcConfig = RpcApplication.getRpcConfig();
                rpcConfig.setRetryStrategy(retryStrategy);
                rpcConfig.setTolerantStrategy(tolerantStrategy);
                // 为属性生成代理对象
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败", e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
