package es.ulpgc.dacd.business;

import es.ulpgc.dacd.business.api.BusinessApi;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import es.ulpgc.dacd.business.reader.EventStoreReader;
import es.ulpgc.dacd.business.subscriber.BusinessSubscriber;

public class Main {
    public static void main(String[] args) {
        String dbPath = args.length > 0 ? args[0] : "datamart.db";
        String eventStorePath = args.length > 1 ? args[1] : "./eventstore";
        String brokerUrl = args.length > 2 ? args[2] : "tcp://localhost:61616";
        int port = args.length > 3 ? Integer.parseInt(args[3]) : 8081;

        SQLiteDatamart datamart = new SQLiteDatamart(dbPath);

        EventStoreReader reader = new EventStoreReader(datamart, eventStorePath);
        reader.loadHistory();

        BusinessSubscriber subscriber = new BusinessSubscriber(datamart, brokerUrl);
        try {
            subscriber.start();
        } catch (Exception e) {
            System.err.println("Error conectando a ActiveMQ: " + e.getMessage());
        }

        BusinessApi api = new BusinessApi(datamart, port);
        api.start();
    }
}