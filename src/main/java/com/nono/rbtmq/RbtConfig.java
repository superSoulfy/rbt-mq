package com.nono.rbtmq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Configuration
public class RbtConfig{

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("192.168.1.70");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("tvhost");
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory());
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback(){
            @Override
            public void confirm(CorrelationData correlationData,boolean ack,String cause){
                if(ack){
                    System.out.println(" 提交成功 CorrelationData：" + correlationData + " cause:" + cause);
                }else{
                    System.out.println(" 提交失败 CorrelationData：" + correlationData + " cause:" + cause);
                }
            }
        });
        return rabbitTemplate;
    }

    @Value("${qu}")
    public String queues;

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleMessageListenerContainer.setPrefetchCount(1);
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory());
        simpleMessageListenerContainer.setExposeListenerChannel(true);
        simpleMessageListenerContainer.setQueueNames(queues.split(","));
        simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener(){
            @Override
            public void onMessage(Message message,Channel channel) throws Exception{
                System.out.println("consume queue:【"+message.getMessageProperties().getConsumerQueue()+"】 message:【"+new String(message.getBody(),StandardCharsets.UTF_8)+"】");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }
        });
        return simpleMessageListenerContainer;
    }

}
