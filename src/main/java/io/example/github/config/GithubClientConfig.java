package io.example.github.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubClientConfig {

    @Value("${github.base-url}")
    private String baseUrl;

    @Value("${github.token:}")
    private String token;

    @Bean
    public RestClient githubRestClient() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28");

        if (token != null && !token.isBlank()) {
            builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return builder.build();
    }
}
