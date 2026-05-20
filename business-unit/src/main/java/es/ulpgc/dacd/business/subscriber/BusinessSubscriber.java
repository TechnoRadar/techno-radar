package es.ulpgc.dacd.business.subscriber;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class BusinessSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(BusinessSubscriber.class);
    private final String brokerUrl;
    private final String topicName;
    private final SQLiteDatamart datamart;

    public BusinessSubscriber(String brokerUrl, String topicName, SQLiteDatamart datamart) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.datamart = datamart;
    }

    public void start() {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = connectionFactory.createConnection();
            connection.setClientID("business-unit-subscriber");
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createConsumer(topic);

            logger.info("Suscrito a ActiveMQ en el topic: {}", topicName);

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        String jsonEvent = ((TextMessage) message).getText();
                        processEvent(jsonEvent);
                    }
                } catch (JMSException e) {
                    logger.error("Error procesando mensaje JMS", e);
                }
            });
        } catch (JMSException e) {
            logger.error("Error conectando a ActiveMQ", e);
        }
    }

    private void processEvent(String jsonEvent) {
        try {
            JsonObject root = JsonParser.parseString(jsonEvent).getAsJsonObject();
            String sourceSystem = root.get("ss").getAsString().toLowerCase();
            JsonObject payload = root.getAsJsonObject("payload");

            if (sourceSystem.contains("github")) {
                String name = payload.get("language").getAsString();
                int stars = payload.get("stars").getAsInt();

                datamart.updateTrend(name, stars, 0);

            } else if (sourceSystem.contains("stackexchange")) {
                String name = payload.get("tagName").getAsString();
                int count = payload.get("count").getAsInt();

                datamart.updateTrend(name, 0, count);
            }
        } catch (Exception e) {
            logger.error("Error al parsear el evento JSON: {}", jsonEvent, e);
        }
    }
}