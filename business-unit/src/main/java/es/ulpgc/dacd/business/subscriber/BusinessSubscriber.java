package es.ulpgc.dacd.business.subscriber;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class BusinessSubscriber implements MessageListener {
    private final SQLiteDatamart datamart;
    private final String brokerUrl;
    private final Gson gson;

    public BusinessSubscriber(SQLiteDatamart datamart, String brokerUrl) {
        this.datamart = datamart;
        this.brokerUrl = brokerUrl;
        this.gson = new Gson();
    }

    public void start() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID("BusinessUnitClient");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic githubTopic = session.createTopic("Github.Trends");
        MessageConsumer githubConsumer = session.createDurableSubscriber(githubTopic, "BusinessUnit-Github");
        githubConsumer.setMessageListener(this);

        Topic seTopic = session.createTopic("StackExchange.Trends");
        MessageConsumer seConsumer = session.createDurableSubscriber(seTopic, "BusinessUnit-StackExchange");
        seConsumer.setMessageListener(this);

        System.out.println("🔥 Business Subscriber conectado a ActiveMQ y escuchando en tiempo real...");
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String json = textMessage.getText();
                processEvent(json);
            }
        } catch (JMSException e) {
            System.err.println("Error procesando mensaje JMS: " + e.getMessage());
        }
    }

    private void processEvent(String jsonLine) {
        try {
            JsonObject event = gson.fromJson(jsonLine, JsonObject.class);
            if (!event.has("payload")) return;

            JsonObject payload = event.getAsJsonObject("payload");

            if (payload.has("repositoryName")) {
                String language = payload.has("language") && !payload.get("language").isJsonNull()
                        ? payload.get("language").getAsString() : "unknown";
                int stars = payload.has("stars") ? payload.get("stars").getAsInt() : 0;
                datamart.updateGithubTrend(language, stars);
                System.out.println("✅ [Tiempo Real] Actualizado GitHub: " + language + " (" + stars + " estrellas)");

            } else if (payload.has("tagName")) {
                String tag = payload.get("tagName").getAsString();
                int count = payload.has("count") ? payload.get("count").getAsInt() : 0;
                datamart.updateStackExchangeTrend(tag, count);
                System.out.println("✅ [Tiempo Real] Actualizado StackExchange: " + tag + " (" + count + " preguntas)");
            }
        } catch (Exception e) {
            System.err.println("Error parseando JSON en tiempo real: " + e.getMessage());
        }
    }
}
