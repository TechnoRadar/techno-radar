package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.StackExchangeClient;
import es.ulpgc.dacd.control.StackExchangeFeeder;
import es.ulpgc.dacd.persistence.JmsStackExchangeStore;
import es.ulpgc.dacd.persistence.StackExchangeStore;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4){
            System.err.println("Error: Configuración incompleta.");
            System.err.println("Uso esperado: java Main <baseUrl> <brokerUrl> <topicName> <sourceSystem>");
            System.err.println("Ejemplo: java Main https://api.stackexchange.com/2.3 tcp://localhost:61616 stackexchange.trends stackexchange-feeder");
            return;
        }

        String baseUrl = args[0];
        String brokerUrl = args[1];
        String topicName = args[2];
        String sourceSystem = args[3];

        System.out.println("Iniciando Techno-Radar (Módulo StackExchange)...");
        System.out.println("-> Conectando a Broker: " + brokerUrl);

        StackExchangeFeeder feeder = new StackExchangeClient(baseUrl);
        StackExchangeStore store = new JmsStackExchangeStore(brokerUrl, topicName, sourceSystem);

        new Controller(feeder, store).start();
    }
}
