package es.ulpgc.dacd;

import es.ulpgc.dacd.subscriber.EventStoreSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.error("Uso: java Main <brokerUrl> <topic1,topic2,...>");
            return;
        }

        String brokerUrl = args[0];
        String[] topicNames = args[1].split(",");
        List<EventStoreSubscriber> subscribers = new ArrayList<>();

        logger.info("Iniciando Event Store Builder...");

        for (String topicName : topicNames) {
            String clientId = "event-store-builder-" + topicName.trim();
            try {
                EventStoreSubscriber subscriber = new EventStoreSubscriber(brokerUrl, topicName.trim(), clientId);
                subscriber.start();
                subscribers.add(subscriber);
                logger.info("Suscripción durable establecida para el topic: {}", topicName);
            } catch (Exception e) {
                logger.error("Error al suscribirse al topic {}", topicName, e);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Cerrando suscriptores...");
            for (EventStoreSubscriber sub : subscribers) {
                try { sub.close(); } catch (Exception ignored) {}
            }
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("Hilo principal interrumpido. Saliendo...");
        }
    }
}