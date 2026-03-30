package es.ulpgc.dacd.control;

import es.ulpgc.dacd.model.GitHubTrend;
import es.ulpgc.dacd.persistence.GitHubStore;
import java.util.List;

public class Controller {
    private final GitHubFeeder feeder;
    private final GitHubStore store;

    public Controller(GitHubFeeder feeder, GitHubStore store) {
        this.feeder = feeder;
        this.store = store;
    }

    public void execute() {
        List<GitHubTrend> trends = feeder.getTrends();
        if (trends != null) {
            for (GitHubTrend trend : trends) {
                store.save(trend);
            }
        }
    }
}
