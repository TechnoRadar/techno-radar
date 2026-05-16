package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.GitHubClient;
import es.ulpgc.dacd.control.GitHubFeeder;
import es.ulpgc.dacd.model.GitHubTrend;
import es.ulpgc.dacd.persistence.JmsEventStore;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4){
            System.err.println("Error: Configuración incompleta.");
            System.err.println("Uso esperado: java Main <baseUrl> <brokerUrl> <topicName> <sourceSystem>");
            return;
        }

        String baseUrl = args[0];
        String brokerUrl = args[1];
        String topicName = args[2];
        String sourceSystem = args[3];

        System.out.println("Iniciando Techno-Radar (Módulo GitHub)...");
        System.out.println("-> Conectando a Broker: " + brokerUrl);
        System.out.println("-> Publicando en Topic: " + topicName);

        GitHubFeeder feeder = new GitHubClient(baseUrl);

        JmsEventStore<GitHubTrend> store = new JmsEventStore<>(brokerUrl, topicName, sourceSystem);

        new Controller(feeder, store).start();
    }
}