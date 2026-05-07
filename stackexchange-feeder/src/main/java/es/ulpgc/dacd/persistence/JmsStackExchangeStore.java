package es.ulpgc.dacd.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.dacd.model.Event;
import es.ulpgc.dacd.model.StackExchangeTrend;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.time.Instant;

public class JmsStackExchangeStore implements StackExchangeStore, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(JmsStackExchangeStore.class);

    private final String topicName;
    private final String sourceSystem;
    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private final ObjectMapper objectMapper;

    public JmsStackExchangeStore(String brokerUrl, String topicName, String sourceSystem) {
        this.topicName = topicName;
        this.sourceSystem = sourceSystem;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // CRÍTICO: Desactivar timestamps numéricos para usar el estándar ISO-8601
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
            logger.error("Error crítico: No se pudo conectar a ActiveMQ", e);
        }
    }

    @Override
    public void save(StackExchangeTrend trend) {
        try {
            // 1. Convertimos el objeto interno a String (Texto Plano)
            String payloadPlano = objectMapper.writeValueAsString(trend);

            // 2. Creamos el Evento usando Instant
            Event event = new Event(Instant.now(), this.sourceSystem, payloadPlano);

            // 3. Serializamos el evento completo a JSON
            String json = objectMapper.writeValueAsString(event);
            TextMessage message = session.createTextMessage(json);
            producer.send(message);

            logger.info("Evento StackExchange enviado al broker: {}", json);
        } catch (JsonProcessingException | JMSException e) {
            logger.error("Error al serializar/enviar el evento", e);
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