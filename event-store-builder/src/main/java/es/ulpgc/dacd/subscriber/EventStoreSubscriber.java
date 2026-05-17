package es.ulpgc.dacd.subscriber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventStoreSubscriber implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreSubscriber.class);

    private final Connection connection;
    private final Session session;
    private final MessageConsumer consumer;
    private static final String BASE_DIR = "eventstore";
    private final ObjectMapper mapper = new ObjectMapper();

    public EventStoreSubscriber(String brokerUrl, String topicName, String clientId) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        this.connection = factory.createConnection();
        this.connection.setClientID(clientId);
        this.connection.start();

        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic destination = session.createTopic(topicName);
        this.consumer = session.createDurableSubscriber(destination, clientId + "-sub");
    }

    public void start() throws JMSException {
        logger.info("Suscripción duradera iniciada. Escuchando eventos...");

        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage textMessage) {
                try {
                    String jsonEvent = textMessage.getText();
                    String destinationInfo = message.getJMSDestination().toString();
                    processMessage(jsonEvent, destinationInfo);
                } catch (JMSException | IOException e) {
                    logger.error("Error procesando mensaje: ", e );
                }
            }
        });
    }

    private void processMessage(String jsonEvent, String destinationInfo) throws IOException {
        if (destinationInfo.contains("ActiveMQ.Advisory")) {
            return;
        }

        JsonNode rootNode = mapper.readTree(jsonEvent);
        String tsIso = rootNode.get("ts").asText();
        String source = rootNode.get("ss").asText();

        Instant instant = Instant.parse(tsIso);
        String dateString = instant.atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String topic = destinationInfo.replace("topic://", "");

        saveToFile(topic, source, dateString, jsonEvent);
    }

    private void saveToFile(String topic, String ss, String dateString, String eventData) throws IOException {
        String path = BASE_DIR + File.separator + topic + File.separator + ss;
        File dir = new File(path);

        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear la estructura de directorios: " + path);
        }

        File eventFile = new File(dir, dateString + ".events");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(eventFile, true))) {
            writer.write(eventData);
            writer.newLine();
        }
    }

    @Override
    public void close() throws JMSException {
        if (consumer != null) consumer.close();
        if (session != null) session.close();
        if (connection != null) connection.close();
        logger.info("Recursos del Subscriber cerrados.");
    }
}