package es.ulpgc.dacd.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record StackExchangeTrend(
        @JsonProperty("tagName")
        String name,
        int count,
        Instant capturedAt
) {}