package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.GitHubTrend;
import es.ulpgc.dacd.persistence.GitHubStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private final GitHubFeeder feeder;
    private final GitHubStore store;
    private static final long PERIOD = 10000;

    public Controller(GitHubFeeder feeder, GitHubStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void start() {
        logger.info("Iniciando captura periódica (GitHub)...");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                execute();
            }
        }, 0, PERIOD);
    }

    private void execute() {
        try {
            List<GitHubTrend> trends = feeder.getTrends();
            if (trends != null && !trends.isEmpty()) {
                for (GitHubTrend trend : trends) {
                    store.save(trend);
                }
                logger.info("Actualización completada y enviada a ActiveMQ. Total procesados: {}", trends.size());
            } else {
                logger.warn("No se obtuvieron tendencias de la API en este ciclo.");
            }
        } catch (Exception e) {
            logger.error("Error durante la ejecución del controlador", e);
        }
    }
}
