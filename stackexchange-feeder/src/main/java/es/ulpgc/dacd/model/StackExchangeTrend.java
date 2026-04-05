package es.ulpgc.dacd.model;

import java.time.Instant;

public record StackExchangeTrend(
        String name,
        int count,
        Instant capturedAt
) {}