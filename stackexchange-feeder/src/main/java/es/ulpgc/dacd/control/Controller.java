package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.StackExchangeTrend;
import es.ulpgc.dacd.persistence.StackExchangeStore;
import java.util.List;

public class Controller {
    private final StackExchangeFeeder feeder;
    private final StackExchangeStore store;

    public Controller(StackExchangeFeeder feeder, StackExchangeStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void execute() {
        List<StackExchangeTrend> trends = feeder.getTrends();
        if (trends != null) {
            for (StackExchangeTrend trend : trends) {
                store.save(trend);
            }
        }
    }
}