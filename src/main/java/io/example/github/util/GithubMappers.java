package io.example.github.util;

import io.example.github.model.CommitInfo;
import io.example.github.model.RepoSummary;
import org.springframework.http.HttpHeaders;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubMappers {

    @SuppressWarnings("unchecked")
    public static RepoSummary mapRepo(Map<String, Object> json) {
        RepoSummary r = new RepoSummary();
        r.setName((String) json.get("name"));
        r.setFullName((String) json.get("full_name"));
        r.setPrivate(Boolean.TRUE.equals(json.get("private")));
        r.setFork(Boolean.TRUE.equals(json.get("fork")));
        r.setHtmlUrl((String) json.get("html_url"));
        r.setDefaultBranch((String) json.get("default_branch"));
        return r;
    }

    @SuppressWarnings("unchecked")
    public static CommitInfo mapCommit(Map<String, Object> json) {
        CommitInfo c = new CommitInfo();
        c.setSha((String) json.get("sha"));
        c.setHtmlUrl((String) json.get("html_url"));
        Map<String, Object> commit = (Map<String, Object>) json.get("commit");
        if (commit != null) {
            c.setMessage((String) commit.get("message"));
            Map<String, Object> author = (Map<String, Object>) commit.get("author");
            if (author != null) {
                c.setAuthorName((String) author.get("name"));
                c.setAuthorEmail((String) author.get("email"));
                String date = (String) author.get("date");
                if (date != null) {
                    c.setTimestamp(OffsetDateTime.parse(date));
                }
            }
        }
        return c;
    }

    // Parse the RFC 5988 Link header; return next URL if present
    public static String parseNextLink(HttpHeaders headers) {
        String link = headers.getFirst("Link");
        if (link == null) return null;

        // pattern: <url>; rel="next", <url>; rel="last"
        Pattern p = Pattern.compile("<([^>]+)>;\\s*rel=\"([^\"]+)\"");
        Matcher m = p.matcher(link);
        Map<String, String> rels = new HashMap<>();
        while (m.find()) {
            rels.put(m.group(2), m.group(1));
        }
        return rels.get("next");
    }

    // Parse the RFC 5988 Link header and extract pagination info
    public static PaginationInfo parsePaginationInfo(HttpHeaders headers) {
        String link = headers.getFirst("Link");
        if (link == null) return new PaginationInfo(null, null, 0);

        // pattern: <url>; rel="next", <url>; rel="last"
        Pattern p = Pattern.compile("<([^>]+)>;\\s*rel=\"([^\"]+)\"");
        Matcher m = p.matcher(link);
        Map<String, String> rels = new HashMap<>();
        while (m.find()) {
            rels.put(m.group(2), m.group(1));
        }
        
        String nextUrl = rels.get("next");
        String lastUrl = rels.get("last");
        int totalPages = 0;
        
        if (lastUrl != null) {
            // Extract page number from last URL: .../repos?page=5&per_page=30
            Pattern pagePattern = Pattern.compile("page=(\\d+)");
            Matcher pageMatcher = pagePattern.matcher(lastUrl);
            if (pageMatcher.find()) {
                totalPages = Integer.parseInt(pageMatcher.group(1));
            }
        }
        
        return new PaginationInfo(nextUrl, lastUrl, totalPages);
    }

    public static class PaginationInfo {
        private final String nextUrl;
        private final String lastUrl;
        private final int totalPages;

        public PaginationInfo(String nextUrl, String lastUrl, int totalPages) {
            this.nextUrl = nextUrl;
            this.lastUrl = lastUrl;
            this.totalPages = totalPages;
        }

        public String getNextUrl() {
            return nextUrl;
        }

        public String getLastUrl() {
            return lastUrl;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean hasNext() {
            return nextUrl != null;
        }
    }
}
