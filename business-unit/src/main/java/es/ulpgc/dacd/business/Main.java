package es.ulpgc.dacd.business;

import es.ulpgc.dacd.business.api.BusinessApi;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import es.ulpgc.dacd.business.reader.EventStoreReader;
import es.ulpgc.dacd.business.subscriber.BusinessSubscriber;

public class Main {
    public static void main(String[] args) {
        SQLiteDatamart datamart = new SQLiteDatamart("datamart.db");

        String eventStorePath = "C:\\Users\\User\\IdeaProjects\\techno-radar\\eventstore";
        EventStoreReader reader = new EventStoreReader(datamart, eventStorePath);
        reader.loadHistory();

        String brokerUrl = "tcp://localhost:61616";
        BusinessSubscriber subscriber = new BusinessSubscriber(datamart, brokerUrl);
        try {
            subscriber.start();
        } catch (Exception e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
        }

        BusinessApi api = new BusinessApi(datamart, 8081);
        api.start();
    }
}