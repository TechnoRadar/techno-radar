package es.ulpgc.dacd.persistence;

import com.google.gson.Gson;
import es.ulpgc.dacd.model.Event;
import es.ulpgc.dacd.model.GitHubTrend;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.time.Instant;

public class JmsGitHubStore implements GitHubStore {
    private final String topicName;
    private final String sourceSystem;
    private final Gson gson;
    private Session session;
    private MessageProducer producer;

    public JmsGitHubStore(String brokerUrl, String topicName, String sourceSystem) {
        this.topicName = topicName;
        this.sourceSystem = sourceSystem;
        this.gson = new Gson();
        connectToBroker(brokerUrl);
    }

    private void connectToBroker(String brokerUrl) {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection();
            connection.start();

            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic destination = session.createTopic(topicName);
            this.producer = session.createProducer(destination);
        } catch (JMSException e) {
            System.err.println("Error crítico: No se pudo conectar a ActiveMQ. " + e.getMessage());
        }
    }

    @Override
    public void save(GitHubTrend trend) {
        try {
            Event event = new Event(Instant.now().toString(), sourceSystem, trend);

            String jsonEvent = gson.toJson(event);

            TextMessage message = session.createTextMessage(jsonEvent);
            producer.send(message);

        } catch (JMSException e) {
            System.err.println("Error publicando evento en ActiveMQ: " + e.getMessage());
        }
    }
}
