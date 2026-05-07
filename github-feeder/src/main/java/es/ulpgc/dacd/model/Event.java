package es.ulpgc.dacd.model;

import java.time.Instant;

public record Event(Instant ts, String source, String payload) {
}
