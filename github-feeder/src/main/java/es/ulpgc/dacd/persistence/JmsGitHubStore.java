package es.ulpgc.dacd.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.dacd.model.Event;
import es.ulpgc.dacd.model.GitHubTrend;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class JmsGitHubStore implements GitHubStore, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(JmsGitHubStore.class);

    private final String topicName;
    private final String sourceSystem;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private final ObjectMapper objectMapper;

    public JmsGitHubStore(String brokerUrl, String topicName, String sourceSystem) {
        this.topicName = topicName;
        this.sourceSystem = sourceSystem;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        connectToBroker(brokerUrl);
    }

    private void connectToBroker(String brokerUrl) {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            this.connection = factory.createConnection();
            this.connection.start();

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic destination = session.createTopic(topicName);
            this.producer = session.createProducer(destination);

            logger.info("Conectado a ActiveMQ en el topic: {}", topicName);
        } catch (JMSException e) {
            logger.error("Error crítico: No se pudo conectar a ActiveMQ", e);
        }
    }

    @Override
    public void save(GitHubTrend trend) {
        try {
            long timestamp = trend.capturedAt().toEpochMilli();
            Event event = new Event(timestamp, this.sourceSystem, trend);

            String json = objectMapper.writeValueAsString(event);
            TextMessage message = session.createTextMessage(json);
            producer.send(message);

            logger.info("Evento enviado al broker: {}", json);
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar el evento", e);
        } catch (JMSException e) {
            logger.error("Error de JMS al enviar", e);
        }
    }

    @Override
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
            logger.info("Recursos de ActiveMQ cerrados correctamente.");
        } catch (JMSException e) {
            logger.error("Error al cerrar ActiveMQ", e);
        }
    }
}