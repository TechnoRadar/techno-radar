package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.StackExchangeTrend;
import java.util.List;

public interface StackExchangeFeeder {
    List<StackExchangeTrend> getTrends();
}