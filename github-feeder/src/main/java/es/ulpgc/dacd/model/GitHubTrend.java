package es.ulpgc.dacd.model;

import java.time.Instant;

public record GitHubTrend(
        String repositoryName,
        int stars,
        String language,
        Instant capturedAt
) {}
