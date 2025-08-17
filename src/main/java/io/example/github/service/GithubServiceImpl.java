package io.example.github.service;

import io.example.github.client.GithubClient;
import io.example.github.model.CommitInfo;
import io.example.github.model.Page;
import io.example.github.model.RepoActivity;
import io.example.github.model.RepoSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableAsync
@Service
public class GithubServiceImpl implements GithubService {

    private static final Logger log = LoggerFactory.getLogger(GithubServiceImpl.class);
    private final GithubClient client;
    private final ExecutorService executor = Executors.newFixedThreadPool(10); // tune pool size

    public GithubServiceImpl(GithubClient client) {
        this.client = client;
    }

    @Override
    public List<RepoActivity> fetchActivity(String username, int commitLimit) {
        log.info("Fetching activity for username: {} with commit limit: {}", username, commitLimit);

        List<RepoSummary> repos = client.fetchAllRepos(username);
        log.debug("Fetched {} repositories for username: {}", repos.size(), username);

        List<RepoActivity> results = new ArrayList<>();
        for (RepoSummary r : repos) {
            log.debug("Fetching commits for repository: {}", r.getName());
            List<CommitInfo> commits = client.fetchRecentCommits(username, r.getName(), commitLimit);
            results.add(new RepoActivity(r, commits));
        }

        log.info("Successfully fetched activity for {} repositories for username: {}", results.size(), username);
        return results;
    }

    @Override
    public List<RepoActivity> fetchActivityAsync(String username, int limit) {
        log.info("Fetching activity for username: {} with commit limit: {}", username, limit);
        
        List<RepoSummary> repos = client.fetchAllRepos(username);
        log.debug("Fetched {} repositories for username: {}", repos.size(), username);

        List<CompletableFuture<RepoActivity>> futures = repos.stream()
                .map(repo -> CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching commits for repository: {} asynchronously", repo.getName());
                    List<CommitInfo> commits = client.fetchRecentCommits(username, repo.getName(), limit);
                    return new RepoActivity(repo, commits);
                }, executor))
                .toList();

        // Wait for all tasks to finish
        List<RepoActivity> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        log.info("Successfully fetched activity for {} repositories for username: {}", results.size(), username);
        return results;
    }

    @Override
    public Page<RepoActivity> fetchActivityAsync(String username, int limit, int page, int size) {
        log.info("Fetching paginated activity for username: {} with commit limit: {}, page: {}, size: {}", 
                username, limit, page, size);
        
        // First, get all repositories
        List<RepoSummary> repos = client.fetchAllRepos(username);
        log.debug("Fetched {} repositories for username: {}", repos.size(), username);

        // Calculate pagination boundaries
        int totalElements = repos.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        // Validate page bounds
        if (startIndex >= totalElements) {
            log.warn("Requested page {} is out of bounds for {} total elements", page, totalElements);
            return new Page<>(new ArrayList<>(), page, size, totalElements);
        }

        // Get the subset of repositories for this page
        List<RepoSummary> pageRepos = repos.subList(startIndex, endIndex);
        log.debug("Processing page {}: repositories {} to {} of {}", page, startIndex, endIndex - 1, totalElements);

        // Fetch activity for the repositories in this page
        List<CompletableFuture<RepoActivity>> futures = pageRepos.stream()
                .map(repo -> CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching commits for repository: {} asynchronously", repo.getName());
                    List<CommitInfo> commits = client.fetchRecentCommits(username, repo.getName(), limit);
                    return new RepoActivity(repo, commits);
                }, executor))
                .toList();

        // Wait for all tasks to finish
        List<RepoActivity> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        log.info("Successfully fetched paginated activity: {} repositories for username: {} (page {} of {})", 
                results.size(), username, page, (int) Math.ceil((double) totalElements / size));
        
        return new Page<>(results, page, size, totalElements);
    }
}

