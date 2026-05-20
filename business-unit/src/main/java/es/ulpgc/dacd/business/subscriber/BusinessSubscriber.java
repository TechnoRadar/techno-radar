package es.ulpgc.dacd.business.subscriber;

import es.ulpgc.dacd.business.util.EventProcessor;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class BusinessSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(BusinessSubscriber.class);
    private final String brokerUrl;
    private final String topicName;
    private final EventProcessor eventProcessor;
    private Connection connection;

    public BusinessSubscriber(String brokerUrl, String topicName, EventProcessor eventProcessor) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.eventProcessor = eventProcessor;
    }

    public void start() {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.setClientID("business-unit-subscriber");
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(topic);

            logger.info("Suscrito a ActiveMQ en el topic: {}", topicName);

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        eventProcessor.process(((TextMessage) message).getText());
                    }
                } catch (JMSException e) {
                    logger.error("Error procesando mensaje JMS", e);
                }
            });
        } catch (JMSException e) {
            logger.error("Error conectando a ActiveMQ", e);
        }
    }

    public void stop() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            logger.error("Error cerrando conexion de ActiveMQ", e);
        }
    }
}