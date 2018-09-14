package com.yukari.producer;

import com.yukari.entity.RadioMQModel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RadioMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send (RadioMQModel radio) {
        CorrelationData correlationData = new CorrelationData(); // 消息唯一id
        correlationData.setId(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("gift_radio-exchange","gift_radio.A",radio,correlationData);
    }


}
