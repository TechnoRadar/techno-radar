package es.ulpgc.dacd.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record GitHubTrend(
        @JsonProperty("repositoryName")
        String name,

        int stars,
        String language,
        Instant capturedAt
) {}
