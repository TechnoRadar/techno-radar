package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.GitHubTrend;
import es.ulpgc.dacd.persistence.GitHubStore;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private final GitHubFeeder feeder;
    private final GitHubStore store;
    private static final long PERIOD = 3600000;

    public Controller(GitHubFeeder feeder, GitHubStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void execute() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Iniciando captura periódica (GitHub)...");
                    List<GitHubTrend> trends = feeder.getTrends();
                    if (trends != null) {
                        for (GitHubTrend trend : trends) {
                            store.save(trend);
                        }
                        System.out.println("Actualización completada con éxito.");
                    }
                } catch (Exception e) {
                    System.err.println("Error durante la ejecución (GitHub): " + e.getMessage());
                }
            }
        }, 0, PERIOD);
    }
}
