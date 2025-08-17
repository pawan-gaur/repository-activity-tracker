package io.example.github.client;

import io.example.github.model.CommitInfo;
import io.example.github.model.RepoSummary;
import io.example.github.util.GithubMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class GithubClient {

    private static final Logger log = LoggerFactory.getLogger(GithubClient.class);

    private final RestClient rest;

    public GithubClient(RestClient githubRestClient) {
        this.rest = githubRestClient;
    }

    public List<RepoSummary> fetchAllRepos(String username) {
        // Try user repos, then org repos if 404.
        List<RepoSummary> repos = tryPaged("/users/{username}/repos", username);
        if (repos.isEmpty()) {
            repos = tryPaged("/orgs/{username}/repos", username);
        }
        return repos;
    }

    private List<RepoSummary> tryPaged(String path, String username) {
        List<RepoSummary> acc = new ArrayList<>();
        String url = path + "?per_page=100&page=1&sort=updated";
        url = url.replace("{username}", username);
        while (url != null) {
            ResponseEntity<Map[]> response = rest.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(Map[].class);
            Map[] body = response.getBody();
            if (body == null || body.length == 0) break;
            Arrays.stream(body)
                    .map(m -> GithubMappers.mapRepo((Map<String, Object>) m))
                    .forEach(acc::add);
            String next = GithubMappers.parseNextLink(response.getHeaders());
            url = next != null ? URI.create(next).getPath() + "?" + (URI.create(next).getQuery() == null ? "" : URI.create(next).getQuery()) : null;
        }
        return acc;
    }

    public List<CommitInfo> fetchRecentCommits(String username, String repo, int limit) {
        int perPage = Math.min(100, Math.max(1, limit));
        String url = String.format("/repos/%s/%s/commits?per_page=%d", username, repo, perPage);

        try {
            ResponseEntity<Map[]> response = rest.get().uri(url).retrieve().toEntity(Map[].class);
            Map[] body = response.getBody();
            List<CommitInfo> commits = new ArrayList<>();
            if (body != null) {
                for (Map m : body) {
                    commits.add(GithubMappers.mapCommit((Map<String, Object>) m));
                    if (commits.size() >= limit) break;
                }
            }
            return commits;
        } catch (org.springframework.web.client.HttpClientErrorException ex) {
            // Handle empty repo (409)
            if (ex.getStatusCode().value() == 409 &&
                    ex.getResponseBodyAsString().contains("Git Repository is empty")) {
                return new ArrayList<>();
            }
            throw ex; // rethrow other errors
        }
    }
}
