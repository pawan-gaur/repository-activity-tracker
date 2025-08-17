# GitHub Repository Activity Connector (Spring Boot, Java 17)

Fetch public repositories for a given GitHub **user or organization**, and for each repository retrieve the **last N commits** (default 20) with **pagination support**.

## Features
- Personal Access Token (classic or fine-grained) authentication
- Handles users **and** orgs (tries both endpoints)
- **Pagination support** for repository activities with customizable page size
- Per-repo fetch of recent commits (default `limit=20`)
- Graceful error handling (rate limits ⇒ HTTP 429 with `retryAfter`)
- **REST API** and **CLI** usage
- Java 17, Spring Boot 3, synchronous `RestClient`
- Asynchronous processing for improved performance

## Quick Start

### 1) Configure Token
Create a GitHub Personal Access Token and export it (recommended):

```bash
export GITHUB_TOKEN=ghp_xxx...   # or fine-grained token
```

Alternatively, set it in `src/main/resources/application.yml` (not recommended).

### 2) Build & Run
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--username=octocat"
```
Or build a jar:
```bash
./mvnw -q -DskipTests package
java -jar target/github-activity-connector-0.0.1.jar --username=octocat
```

### 3) REST API
Start the app (without CLI) and call the endpoint:
```bash
java -jar target/github-activity-connector-0.0.1.jar --no-cli
curl "http://localhost:8080/api/github/activity/octocat?page=0&size=10&limit=20"
```

## API Documentation

### Get Repository Activity with Pagination

**Endpoint:** `GET /api/github/activity/{username}`

**Path Parameters:**
- `username` (required): GitHub username or organization name

**Query Parameters:**
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Number of repositories per page (1-100, default: 20)
- `limit` (optional): Number of commits per repository (1-100, default: 20)

**Example Requests:**
```bash
# Get first page with 10 repositories, 20 commits each
curl "http://localhost:8080/api/github/activity/octocat?page=0&size=10&limit=20"

# Get second page with 5 repositories, 10 commits each
curl "http://localhost:8080/api/github/activity/octocat?page=1&size=5&limit=10"

# Get all repositories (no pagination)
curl "http://localhost:8080/api/github/activity/octocat?size=100"
```

**Response Format:**
```json
{
  "content": [
    {
      "repository": {
        "name": "Spoon-Knife",
        "fullName": "octocat/Spoon-Knife",
        "private": false,
        "fork": true,
        "htmlUrl": "https://github.com/octocat/Spoon-Knife",
        "defaultBranch": "main"
      },
      "commits": [
        {
          "sha": "abc123",
          "message": "Fix docs",
          "authorName": "The Octocat",
          "authorEmail": "octo@github.com",
          "timestamp": "2024-12-01T10:00:00Z",
          "htmlUrl": "https://github.com/octocat/Spoon-Knife/commit/abc123"
        }
      ]
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "hasNext": true,
  "hasPrevious": false,
  "numberOfElements": 10
}
```

**Pagination Metadata:**
- `content`: Array of repository activities for the current page
- `pageNumber`: Current page number (0-based)
- `pageSize`: Number of repositories per page
- `totalElements`: Total number of repositories
- `totalPages`: Total number of pages
- `hasNext`: Whether there's a next page
- `hasPrevious`: Whether there's a previous page
- `numberOfElements`: Number of repositories in the current page

## Design Notes

- **Pagination**: Repository listing follows `Link` header RFC5988; we parse `rel="next"` to traverse pages until exhausted. The pagination is implemented at the repository level, not commits.
- **Commits**: We request `per_page=limit` and slice to `limit`. (Max 100 per GitHub API.)
- **Users vs Orgs**: We first call `/users/{username}/repos`; if nothing is returned we try `/orgs/{username}/repos`.
- **Rate limits**: If GitHub returns 403/429 we surface `429` to callers with helpful metadata; CLI prints a friendly message.
- **Extensibility**: Client/Service/Controller layers allow swapping in other connectors with the same shape.
- **Asynchronous Processing**: Repository commits are fetched concurrently for improved performance.
- **Custom Pagination**: Implemented without Spring Data dependencies for lightweight deployment.

## Project Layout
```
src/main/java/io/example/github/
├── GithubActivityConnectorApp.java          # Main application class
├── config/
│   └── GithubClientConfig.java             # HTTP client configuration
├── client/
│   └── GithubClient.java                   # GitHub API client
├── service/
│   ├── GithubService.java                  # Service interface
│   └── GithubServiceImpl.java              # Service implementation with pagination
├── controller/
│   └── GithubController.java               # REST API controller with pagination
├── cli/
│   └── ActivityCli.java                    # Command-line interface
├── model/
│   ├── RepoSummary.java                    # Repository metadata
│   ├── CommitInfo.java                     # Commit information
│   ├── RepoActivity.java                   # Repository with commits
│   └── Page.java                           # Custom pagination wrapper
├── util/
│   └── GithubMappers.java                  # JSON mapping utilities
└── exception/
    └── GlobalExceptionHandler.java         # Global error handling
```

## Implementation Details

### Pagination Strategy
The pagination is implemented at the repository level:
1. Fetch all repositories for the user/organization
2. Apply pagination to the repository list
3. Fetch commits for repositories in the current page concurrently
4. Return paginated results with metadata

### Performance Optimizations
- **Concurrent Processing**: Repository commits are fetched asynchronously using `CompletableFuture`
- **Configurable Thread Pool**: Uses a fixed thread pool (default: 10 threads) for concurrent API calls
- **Efficient Pagination**: Only fetches commits for repositories in the requested page

### Error Handling
- **Rate Limiting**: Returns HTTP 429 with retry-after information
- **Invalid Pages**: Returns empty page for out-of-bounds page requests
- **Validation**: Input validation for page, size, and limit parameters
- **Global Exception Handler**: Centralized error handling with consistent response format

## Tests

The project includes comprehensive test coverage for the controller and pagination functionality:

### Test Structure
```
src/test/java/io/example/github/
├── controller/
│   ├── GithubControllerTest.java              # Unit tests for controller
│   └── GithubControllerIntegrationTest.java   # Integration tests with MockMvc
└── model/
    └── PageTest.java                          # Unit tests for pagination model
```

### Test Coverage

#### Controller Tests (`GithubControllerTest.java`)
- ✅ Default parameter handling
- ✅ Custom pagination parameters
- ✅ Empty page responses
- ✅ Multiple repository scenarios
- ✅ Pagination metadata validation
- ✅ Exception handling
- ✅ Edge cases (null/empty usernames, large page numbers)
- ✅ Maximum parameter validation

#### Integration Tests (`GithubControllerIntegrationTest.java`)
- ✅ REST endpoint validation with MockMvc
- ✅ HTTP status code verification
- ✅ JSON response structure validation
- ✅ Query parameter validation
- ✅ Error handling (400, 500 status codes)
- ✅ Organization username handling
- ✅ Special character username handling

#### Model Tests (`PageTest.java`)
- ✅ Pagination metadata calculations
- ✅ Edge cases (empty content, exact divisions)
- ✅ First/last/middle page scenarios
- ✅ Large page number handling
- ✅ Zero total elements handling

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=GithubControllerTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Examples
```java
// Unit test example
@Test
@DisplayName("Should return paginated activity successfully with default parameters")
void getActivity_WithDefaultParameters_ShouldReturnSuccess() {
    // Test implementation
}

// Integration test example
@Test
@DisplayName("Should return 200 OK with paginated response for valid request")
void getActivity_ValidRequest_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/api/github/activity/testuser"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content", hasSize(1)));
}
```

## Docker (optional)
Build an image using Spring Boot Buildpacks:
```bash
./mvnw spring-boot:build-image
docker run -e GITHUB_TOKEN -p 8080:8080 github-activity-connector:0.0.1 --no-cli
```

## Configuration

### Application Properties
```yaml
# src/main/resources/application.yml
github:
  api:
    base-url: https://api.github.com
    timeout: 30s
    retry:
      max-attempts: 3
      backoff: 1s

server:
  port: 8080

logging:
  level:
    io.example.github: DEBUG
```

### Environment Variables
- `GITHUB_TOKEN`: GitHub Personal Access Token
- `SERVER_PORT`: Application port (default: 8080)

## Notes
- Only **public** repositories are fetched with this sample. To include private repos, the token must have the required **repo** scope and the API endpoints would need to use the authenticated user/org visibility parameters.
- This is a learning/demo project; production usage should add retries with jitter, caching, and more robust error mapping.
- The pagination implementation is lightweight and doesn't require additional dependencies.
- Consider implementing caching for frequently accessed repositories to reduce API calls.
```

