package es.ulpgc.dacd;

import es.ulpgc.dacd.control.Controller;
import es.ulpgc.dacd.control.GitHubClient;
import es.ulpgc.dacd.control.GitHubFeeder;
import es.ulpgc.dacd.model.GitHubTrend;
import es.ulpgc.dacd.persistence.JmsEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        if (args.length < 4){
            logger.error("Error: Configuración incompleta.");
            logger.error("Uso esperado: java Main <baseUrl> <brokerUrl> <topicName> <sourceSystem>");
            return;
        }

        String baseUrl = args[0];
        String brokerUrl = args[1];
        String topicName = args[2];
        String sourceSystem = args[3];

        logger.info("Iniciando Techno-Radar (Módulo GitHub)...");
        logger.info("-> Conectando a Broker: {}", brokerUrl);
        logger.info("-> Publicando en Topic: {}", topicName);

        GitHubFeeder feeder = new GitHubClient(baseUrl);

        JmsEventPublisher<GitHubTrend> store = new JmsEventPublisher<>(brokerUrl, topicName, sourceSystem);

        new Controller(feeder, store).start();
    }
}