package vit.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by zadol on 22.02.2019.
 */
@Component
public class Publisher {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${vit.rabbitmq.exchange}")
    private String exchange;

    @Value("${vit.rabbitmq.routingkey}")
    private String routingKey;

    private static Logger logger = LoggerFactory.getLogger(Publisher.class);

    public void produceMsg(String msg){
        amqpTemplate.convertAndSend(exchange, routingKey, msg);
        logger.info("Send msg = " + msg);
    }
}