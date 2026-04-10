package es.ulpgc.dacd.persistence;

import es.ulpgc.dacd.model.GitHubTrend;

public interface GitHubStore {
    void save(GitHubTrend trend);
}
