package io.example.github.controller;

import io.example.github.model.CommitInfo;
import io.example.github.model.Page;
import io.example.github.model.RepoActivity;
import io.example.github.model.RepoSummary;
import io.example.github.service.GithubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubController Tests")
class GithubControllerTest {

    @Mock
    private GithubService githubService;

    @InjectMocks
    private GithubController githubController;

    private RepoActivity sampleRepoActivity;
    private Page<RepoActivity> samplePage;

    @BeforeEach
    void setUp() {
        // Create sample data
        RepoSummary repoSummary = new RepoSummary();
        repoSummary.setName("test-repo");
        repoSummary.setFullName("testuser/test-repo");
        repoSummary.setPrivate(false);
        repoSummary.setFork(false);
        repoSummary.setHtmlUrl("https://github.com/testuser/test-repo");
        repoSummary.setDefaultBranch("main");

        CommitInfo commitInfo = new CommitInfo();
        commitInfo.setSha("abc123");
        commitInfo.setMessage("Test commit");
        commitInfo.setAuthorName("Test User");
        commitInfo.setAuthorEmail("test@example.com");
        commitInfo.setTimestamp(OffsetDateTime.now());
        commitInfo.setHtmlUrl("https://github.com/testuser/test-repo/commit/abc123");

        sampleRepoActivity = new RepoActivity(repoSummary, Arrays.asList(commitInfo));
        samplePage = new Page<>(Arrays.asList(sampleRepoActivity), 0, 20, 1);
    }

    @Test
    @DisplayName("Should return paginated activity successfully with default parameters")
    void getActivity_WithDefaultParameters_ShouldReturnSuccess() {
        // Arrange
        String username = "testuser";
        when(githubService.fetchActivityAsync(eq(username), eq(20), eq(0), eq(20)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, 20, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getNumberOfElements());
        assertEquals(0, response.getBody().getPageNumber());
        assertEquals(20, response.getBody().getPageSize());
        assertEquals(1, response.getBody().getTotalElements());

        verify(githubService, times(1)).fetchActivityAsync(username, 20, 0, 20);
    }

    @Test
    @DisplayName("Should return paginated activity successfully with custom parameters")
    void getActivity_WithCustomParameters_ShouldReturnSuccess() {
        // Arrange
        String username = "testuser";
        int page = 1;
        int size = 10;
        int limit = 5;
        
        when(githubService.fetchActivityAsync(eq(username), eq(limit), eq(page), eq(size)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, page, size, limit);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(githubService, times(1)).fetchActivityAsync(username, limit, page, size);
    }

    @Test
    @DisplayName("Should return empty page when no repositories found")
    void getActivity_WhenNoRepositories_ShouldReturnEmptyPage() {
        // Arrange
        String username = "emptyuser";
        Page<RepoActivity> emptyPage = new Page<>(Arrays.asList(), 0, 20, 0);
        
        when(githubService.fetchActivityAsync(eq(username), eq(20), eq(0), eq(20)))
                .thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, 20, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals(0, response.getBody().getNumberOfElements());
        assertEquals(0, response.getBody().getTotalElements());

        verify(githubService, times(1)).fetchActivityAsync(username, 20, 0, 20);
    }

    @Test
    @DisplayName("Should return multiple repositories in paginated response")
    void getActivity_WithMultipleRepositories_ShouldReturnPaginatedResponse() {
        // Arrange
        String username = "testuser";
        
        // Create multiple repo activities
        RepoSummary repo1 = new RepoSummary();
        repo1.setName("repo1");
        repo1.setFullName("testuser/repo1");
        
        RepoSummary repo2 = new RepoSummary();
        repo2.setName("repo2");
        repo2.setFullName("testuser/repo2");
        
        RepoActivity activity1 = new RepoActivity(repo1, Arrays.asList());
        RepoActivity activity2 = new RepoActivity(repo2, Arrays.asList());
        
        Page<RepoActivity> multiPage = new Page<>(Arrays.asList(activity1, activity2), 0, 20, 2);
        
        when(githubService.fetchActivityAsync(eq(username), eq(20), eq(0), eq(20)))
                .thenReturn(multiPage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, 20, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getNumberOfElements());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(2, response.getBody().getContent().size());

        verify(githubService, times(1)).fetchActivityAsync(username, 20, 0, 20);
    }


    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void getActivity_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String username = "testuser";
        RuntimeException serviceException = new RuntimeException("Service error");
        
        when(githubService.fetchActivityAsync(eq(username), eq(20), eq(0), eq(20)))
                .thenThrow(serviceException);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            githubController.getActivity(username, 0, 20, 20);
        });

        verify(githubService, times(1)).fetchActivityAsync(username, 20, 0, 20);
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void getActivity_WithNullUsername_ShouldHandleGracefully() {
        // Arrange
        String username = null;
        when(githubService.fetchActivityAsync(isNull(), eq(20), eq(0), eq(20)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, 20, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(githubService, times(1)).fetchActivityAsync(null, 20, 0, 20);
    }

    @Test
    @DisplayName("Should handle empty username gracefully")
    void getActivity_WithEmptyUsername_ShouldHandleGracefully() {
        // Arrange
        String username = "";
        when(githubService.fetchActivityAsync(eq(""), eq(20), eq(0), eq(20)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, 20, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(githubService, times(1)).fetchActivityAsync("", 20, 0, 20);
    }

    @Test
    @DisplayName("Should handle large page numbers")
    void getActivity_WithLargePageNumber_ShouldHandleGracefully() {
        // Arrange
        String username = "testuser";
        int largePage = 1000;
        
        when(githubService.fetchActivityAsync(eq(username), eq(20), eq(largePage), eq(20)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, largePage, 20, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(githubService, times(1)).fetchActivityAsync(username, 20, largePage, 20);
    }

    @Test
    @DisplayName("Should handle maximum size parameter")
    void getActivity_WithMaximumSize_ShouldHandleGracefully() {
        // Arrange
        String username = "testuser";
        int maxSize = 100;
        
        when(githubService.fetchActivityAsync(eq(username), eq(20), eq(0), eq(maxSize)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, maxSize, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(githubService, times(1)).fetchActivityAsync(username, 20, 0, maxSize);
    }

    @Test
    @DisplayName("Should handle maximum limit parameter")
    void getActivity_WithMaximumLimit_ShouldHandleGracefully() {
        // Arrange
        String username = "testuser";
        int maxLimit = 100;
        
        when(githubService.fetchActivityAsync(eq(username), eq(maxLimit), eq(0), eq(20)))
                .thenReturn(samplePage);

        // Act
        ResponseEntity<Page<RepoActivity>> response = githubController.getActivity(username, 0, 20, maxLimit);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(githubService, times(1)).fetchActivityAsync(username, maxLimit, 0, 20);
    }
}
