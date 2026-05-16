package es.ulpgc.dacd.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.dacd.model.Event;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.time.Instant;

public class JmsEventStore<T> implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(JmsEventStore.class);

    private final String topicName;
    private final String sourceSystem;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private final ObjectMapper objectMapper;

    public JmsEventStore(String brokerUrl, String topicName, String sourceSystem) {
        this.topicName = topicName;
        this.sourceSystem = sourceSystem;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
            logger.error("Error crítico de conexión a ActiveMQ", e);
        }
    }

    public void save(T data) {
        try {
            Event<T> event = new Event<>(Instant.now(), this.sourceSystem, data);
            String json = objectMapper.writeValueAsString(event);

            TextMessage message = session.createTextMessage(json);
            producer.send(message);

            logger.info("Evento enviado con éxito: {}", json);
        } catch (JsonProcessingException | JMSException e) {
            logger.error("Error al guardar el evento en el broker", e);
        }
    }

    @Override
    public void close() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
            logger.info("Recursos de JMS cerrados.");
        } catch (JMSException e) {
            logger.error("Error al cerrar recursos de JMS", e);
        }
    }
}
