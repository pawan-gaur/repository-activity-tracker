package io.example.github.service;

import io.example.github.model.Page;
import io.example.github.model.RepoActivity;

import java.util.List;

public interface GithubService {

    List<RepoActivity> fetchActivity(String username, int commitLimit);

    List<RepoActivity> fetchActivityAsync(String username, int limit);

    Page<RepoActivity> fetchActivityAsync(String username, int limit, int page, int size);

}
