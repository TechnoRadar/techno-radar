package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.StackExchangeTrend;
import es.ulpgc.dacd.persistence.StackExchangeStore;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private final StackExchangeFeeder feeder;
    private final StackExchangeStore store;
    private static final long PERIOD = 3600000;

    public Controller(StackExchangeFeeder feeder, StackExchangeStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void execute() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Iniciando captura periódica (StackExchange)...");
                    List<StackExchangeTrend> trends = feeder.getTrends();
                    if (trends != null) {
                        for (StackExchangeTrend trend : trends) {
                            store.save(trend);
                        }
                        System.out.println("Actualización completada con éxito.");
                    }
                } catch (Exception e) {
                    System.err.println("Error durante la ejecución (StackExchange): " + e.getMessage());
                }
            }
        }, 0, PERIOD);
    }
}
