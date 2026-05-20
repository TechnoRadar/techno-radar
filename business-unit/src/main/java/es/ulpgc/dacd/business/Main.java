package es.ulpgc.dacd.business;

import es.ulpgc.dacd.business.api.BusinessApi;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import es.ulpgc.dacd.business.reader.EventStoreReader;
import es.ulpgc.dacd.business.subscriber.BusinessSubscriber;
import es.ulpgc.dacd.business.util.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        if (args.length < 5){
            logger.error("Uso esperado: java Main <dbPath> <eventStorePath> <brokerUrl> <topicName> <port>");
            return;
        }
        String dbPath = args[0];
        String eventStorePath = args[1];
        String brokerUrl = args[2];
        String topicName = args[3];
        int port = Integer.parseInt(args[4]);

        SQLiteDatamart datamart = new SQLiteDatamart(dbPath);
        EventProcessor eventProcessor = new EventProcessor(datamart);

        EventStoreReader reader = new EventStoreReader(eventStorePath, eventProcessor);
        reader.processHistoricalEvents();

        BusinessApi api = new BusinessApi(datamart);
        api.start(8081);

        BusinessSubscriber subscriber = new BusinessSubscriber(brokerUrl, topicName, eventProcessor);
        subscriber.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            subscriber.stop();
            api.stop();
            datamart.closeSafe();
        }));
    }
}