package io.example.github.model;

import lombok.Data;

@Data
public class RepoSummary {

    private String name;
    private String fullName;
    private boolean isPrivate;
    private boolean fork;
    private String htmlUrl;
    private String defaultBranch;

}
