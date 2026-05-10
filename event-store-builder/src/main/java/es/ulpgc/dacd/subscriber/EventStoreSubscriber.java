package es.ulpgc.dacd.subscriber;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventStoreSubscriber implements AutoCloseable {
    private final Connection connection;
    private final Session session;
    private final MessageConsumer consumer;
    private static final String BASE_DIR = "eventstore";

    public EventStoreSubscriber(String brokerUrl, String topicName, String clientId) throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        this.connection = factory.createConnection();
        this.connection.setClientID(clientId);
        this.connection.start();

        this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        this.consumer = session.createDurableSubscriber(topic, clientId + "-sub");
    }

    public void start() throws JMSException {
        System.out.println("Suscripción duradera iniciada. Escuchando eventos...");

        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage textMessage) {
                try {
                    String jsonEvent = textMessage.getText();
                    String destination = message.getJMSDestination().toString();
                    processMessage(jsonEvent, destination);
                } catch (JMSException | IOException e) {
                    System.err.println("Error procesando mensaje: " + e.getMessage());
                }
            }
        });
    }

    private void processMessage(String jsonEvent, String destinationInfo) throws IOException {
        if (destinationInfo.contains("ActiveMQ.Advisory")) {
            return;
        }

        JsonObject jsonObject = JsonParser.parseString(jsonEvent).getAsJsonObject();
        String tsIso = jsonObject.get("ts").getAsString();
        String source = jsonObject.get("source").getAsString();

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
        System.out.println("Recursos del Subscriber cerrados.");
    }
}
