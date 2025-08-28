package io.example.github.controller;

import io.example.github.model.Page;
import io.example.github.model.RepoActivity;
import io.example.github.model.RepoSummary;
import io.example.github.service.GithubService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@Validated
public class GithubController {

    private static final Logger log = LoggerFactory.getLogger(GithubController.class);
    private final GithubService githubService;

    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/activity/{username}")
    public ResponseEntity<Page<RepoActivity>> getActivity(
            @PathVariable("username") String username,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        log.info("Received request to fetch activity for username: {}, page: {}, size: {}, limit: {}", 
                username, page, size, limit);

        Page<RepoActivity> data = githubService.fetchActivityAsync(username, limit, page, size);
        
        log.info("Successfully fetched {} repository activities for username: {} (page {} of {})", 
                data.getNumberOfElements(), username, page, data.getTotalPages());
        
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/repo/{username}")
    public ResponseEntity<Page<RepoSummary>> getRepositories(
            @PathVariable("username") String username,
            @RequestParam(name = "per_page", defaultValue = "10") @Min(1) @Max(100) int perPage
    ) {
        log.info("Received request to fetch repositories for username: {} with per_page: {}", 
                username, perPage);

        Page<RepoSummary> data = githubService.fetchRepositoriesWithPagination(username, perPage);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/repo/{username}/page")
    public ResponseEntity<Page<RepoSummary>> getRepositoriesByPage(
            @PathVariable("username") String username,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "per_page", defaultValue = "30") @Min(1) @Max(100) int perPage
    ) {
        log.info("Received request to fetch repositories for username: {} page: {} with per_page: {}", 
                username, page, perPage);

        Page<RepoSummary> data = githubService.fetchRepositoriesByPage(username, page, perPage);
        
        log.info("Successfully fetched {} repositories for username: {} (page {} of {})", 
                data.getNumberOfElements(), username, data.getPageNumber(), data.getTotalPages());
        
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
