package io.example.github.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CommitInfo {

    private String sha;
    private String message;
    private String authorName;
    private String authorEmail;
    private OffsetDateTime timestamp;
    private String htmlUrl;

}
