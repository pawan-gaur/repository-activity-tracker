package io.example.github.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepoActivity {

    private RepoSummary repository;
    private List<CommitInfo> commits;

}
