package es.ulpgc.dacd.model;

import java.time.Instant;

public record Event<T>(Instant ts, String ss, T payload) {
}
