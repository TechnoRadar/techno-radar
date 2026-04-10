package es.ulpgc.dacd.persistence;

import es.ulpgc.dacd.model.StackExchangeTrend;

public interface StackExchangeStore {
    void save(StackExchangeTrend trend);
}