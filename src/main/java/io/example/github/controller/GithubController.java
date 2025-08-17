package io.example.github.controller;

import io.example.github.model.Page;
import io.example.github.model.RepoActivity;
import io.example.github.service.GithubService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
