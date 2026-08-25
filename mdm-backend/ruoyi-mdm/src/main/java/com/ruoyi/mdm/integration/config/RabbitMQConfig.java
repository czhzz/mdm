package com.ruoyi.mdm.integration.config;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 分发通道常量（integration 版）
 *
 * <p>Bean 声明暂由 distribution.config.RabbitMQConfig 提供（旧包待删），
 * 旧包删除时将 @Configuration 与 Bean 声明并回本类。
 *
 * @author ruoyi
 */
public class RabbitMQConfig
{
    /** 分发主题交换机 */
    public static final String EXCHANGE = "mdm.distribution";

    /** 死信交换机 */
    public static final String DLX_EXCHANGE = "mdm.distribution.dlx";

    /** 死信队列 */
    public static final String DLX_QUEUE = "mdm.distribution.dlx.queue";

    /** 路由键前缀：mdm.dist.<objectCode> */
    public static final String ROUTING_KEY_PREFIX = "mdm.dist.";

    /**
     * 动态声明订阅方队列（分发配置启用 MQ 通道时调用）
     */
    public static Map<String, Object> queueArgs(String dlxQueueName)
    {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", dlxQueueName);
        return args;
    }
}
