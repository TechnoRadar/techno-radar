package es.ulpgc.dacd.model;

import java.time.Instant;

public record GitHubTrend(
        String name,
        int stars,
        String language,
        Instant capturedAt
) {}
