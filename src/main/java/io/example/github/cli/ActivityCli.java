package io.example.github.cli;

import io.example.github.model.RepoActivity;
import io.example.github.service.GithubServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivityCli implements CommandLineRunner {

    private final GithubServiceImpl service;

    public ActivityCli(GithubServiceImpl service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        // Usage: java -jar app.jar --username=<name> [--limit=20] [--no-cli]
        String username = null;
        int limit = 20;
        boolean noCli = false;
        for (String a : args) {
            if (a.startsWith("--username=")) username = a.substring("--username=".length());
            if (a.startsWith("--limit=")) limit = Integer.parseInt(a.substring("--limit=".length()));
            if (a.equals("--no-cli")) noCli = true;
        }
        if (noCli || username == null || username.isBlank()) return;

        List<RepoActivity> data = service.fetchActivity(username, limit);
        System.out.printf("Username: %s | Repositories: %d | Limit: %d%n", username, data.size(), limit);
        data.forEach(ra -> {
            System.out.printf("Repo: %s (%s) commits=%d%n",
                ra.getRepository().getFullName(), ra.getRepository().getHtmlUrl(), ra.getCommits().size());
            ra.getCommits().forEach(c -> System.out.printf("  - %s | %s | %s%n",
                c.getSha(), c.getTimestamp(), firstLine(c.getMessage())));
        });
    }

    private String firstLine(String s) {
        if (s == null) return "";
        int idx = s.indexOf('\n');
        return idx >= 0 ? s.substring(0, idx) : s;
    }
}
