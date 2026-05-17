package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.StackExchangeTrend;
import es.ulpgc.dacd.persistence.JmsEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private final StackExchangeFeeder feeder;
    private final JmsEventStore<StackExchangeTrend> store;
    private static final long PERIOD = 3600000;

    public Controller(StackExchangeFeeder feeder, JmsEventStore<StackExchangeTrend> store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void start() {
        logger.info("Iniciando servicio de captura periódica (StackExchange)...");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                execute();
            }
        }, 0, PERIOD);
    }

    public void execute() {
        try {
            List<StackExchangeTrend> trends = feeder.getTrends();
            if (trends != null && !trends.isEmpty()) {
                for (StackExchangeTrend trend : trends) {
                    store.save(trend);
                }
                logger.info("Actualización completada y enviada a ActiveMQ. Procesados: {}", trends.size());
            } else {
                logger.warn("No se obtuvieron tendencias de StackExchange.");
            }
        } catch (Exception e) {
            logger.error("Error durante la ejecución del controlador", e);
        }
    }
}