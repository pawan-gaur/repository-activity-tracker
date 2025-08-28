package io.example.github.model;

import java.util.List;

public class PaginationResult<T> {
    private final List<T> repos;
    private final int totalPages;
    private final int currentPage;
    private final boolean hasNext;

    public PaginationResult(List<T> repos, int totalPages, int currentPage, boolean hasNext) {
        this.repos = repos;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.hasNext = hasNext;
    }

    public List<T> getRepos() {
        return repos;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean hasNext() {
        return hasNext;
    }
}
