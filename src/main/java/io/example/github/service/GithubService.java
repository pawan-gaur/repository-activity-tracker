package io.example.github.service;

import io.example.github.model.Page;
import io.example.github.model.RepoActivity;
import io.example.github.model.RepoSummary;

import java.util.List;

public interface GithubService {

    List<RepoActivity> fetchActivity(String username, int commitLimit);

    List<RepoActivity> fetchActivityAsync(String username, int limit);

    Page<RepoActivity> fetchActivityAsync(String username, int limit, int page, int size);

    Page<RepoSummary> fetchRepositoriesWithPagination(String username, int perPage);

    Page<RepoSummary> fetchRepositoriesByPage(String username, int page, int perPage);

}
