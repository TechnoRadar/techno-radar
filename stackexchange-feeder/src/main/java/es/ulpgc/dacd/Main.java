package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.StackExchangeClient;
import es.ulpgc.dacd.model.StackExchangeTrend;
import es.ulpgc.dacd.persistence.JmsEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        if (args.length < 4){
            logger.error("Uso esperado: java Main <apiUrl> <brokerUrl> <topicName> <sourceSystem>");
            return;
        }

        String apiUrl = args[0];
        String brokerUrl = args[1];
        String topicName = args[2];
        String sourceSystem = args[3];

        logger.info("Iniciando Techno-Radar (Módulo StackExchange)...");
        logger.info("-> Conectando a Broker: {}", brokerUrl);
        logger.info("-> Publicando en Topic: {}", topicName);

        StackExchangeClient feeder = new StackExchangeClient(apiUrl);

        JmsEventStore<StackExchangeTrend> store = new JmsEventStore<>(brokerUrl, topicName, sourceSystem);

        Controller controller = new Controller(feeder, store);
        controller.start();
    }
}