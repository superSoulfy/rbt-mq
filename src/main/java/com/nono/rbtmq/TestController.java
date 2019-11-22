package com.nono.rbtmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController{
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${ex:}")
    public String exchange;
    @Value("${rk:}")
    public String routeKey;

    @PostMapping("/t")
    public Map<String,Object> test(@RequestBody List<String> msgs){
        Map<String,Object> result = new HashMap<>();
        msgs.forEach(msg -> {
            MessageProperties messageProperties= getMessageProperties();
            Message message = new Message(msg.getBytes(StandardCharsets.UTF_8),messageProperties);
            rabbitTemplate.send(exchange,routeKey,message);
        });
        result.put("code",0);
        result.put("msg","success");
        result.put("time",System.currentTimeMillis());
        return result;
    }

    @Value("${msg-ttl:}")
    public String msgTTL;
    @Value("${msg-pri:0}")
    public Integer priority;

    private MessageProperties getMessageProperties(){
        MessageProperties messageProperties= new MessageProperties();
        messageProperties.setPriority(priority);
        messageProperties.setExpiration(msgTTL);
        messageProperties.setTimestamp(new Date());
        return messageProperties;
    }
}
