package es.ulpgc.dacd;

import es.ulpgc.dacd.subscriber.EventStoreSubscriber;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java Main <brokerUrl> <topicName>");
            return;
        }

        String brokerUrl = args[0];
        String topicName = args[1];
        String clientId = "event-store-builder-" + topicName;

        try (EventStoreSubscriber subscriber = new EventStoreSubscriber(brokerUrl, topicName, clientId)) {

            subscriber.start();
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Fallo al iniciar el Event Store Builder: " + e.getMessage());
        }
    }
}