package com.ruoyi.mdm.distribution.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 分发通道配置
 *
 * <p>Topic Exchange `mdm.distribution` + 死信交换机 `mdm.distribution.dlx`。
 * 订阅方队列由配置动态声明（分发配置保存时），此处只声明基础设施。
 *
 * @author ruoyi
 */
@Configuration
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

    @Bean
    public TopicExchange distributionExchange()
    {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange distributionDlxExchange()
    {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列：订阅方消费失败（NACK）的消息进入此队列，定时任务扫描重推
     */
    @Bean
    public Queue distributionDlxQueue()
    {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }

    @Bean
    public Binding distributionDlxBinding()
    {
        return BindingBuilder.bind(distributionDlxQueue()).to(distributionDlxExchange()).with("#");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        return template;
    }

    /** RabbitAdmin 用于动态声明订阅方队列（Spring Boot 不会自动创建） */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory)
    {
        return new RabbitAdmin(connectionFactory);
    }

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