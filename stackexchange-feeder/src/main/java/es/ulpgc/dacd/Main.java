package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.StackExchangeClient;
import es.ulpgc.dacd.model.StackExchangeTrend;
import es.ulpgc.dacd.persistence.JmsEventStore; // Usamos el de common

public class Main {
    public static void main(String[] args) {
        if (args.length < 4){
            System.err.println("Uso esperado: java Main <apiUrl> <brokerUrl> <topicName> <sourceSystem>");
            return;
        }

        String apiUrl = args[0];
        String brokerUrl = args[1];
        String topicName = args[2];
        String sourceSystem = args[3];

        System.out.println("Iniciando Techno-Radar (Módulo StackExchange)...");

        StackExchangeClient feeder = new StackExchangeClient(apiUrl);

        JmsEventStore<StackExchangeTrend> store = new JmsEventStore<>(brokerUrl, topicName, sourceSystem);

        Controller controller = new Controller(feeder, store);
        controller.start();
    }
}