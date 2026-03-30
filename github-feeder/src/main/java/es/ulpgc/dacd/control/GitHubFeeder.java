package es.ulpgc.dacd.control;
import es.ulpgc.dacd.model.GitHubTrend;
import java.util.List;

public interface GitHubFeeder {
    List<GitHubTrend> getTrends();
}
