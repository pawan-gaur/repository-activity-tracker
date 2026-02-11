package io.example.github.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/health")
@Validated
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(GithubController.class);

    @GetMapping("/status")
    public ResponseEntity<?> getHealthStatus() {
        log.info("Received request to get Health Status : {}", LocalDateTime.now());


        return new ResponseEntity<>("Health OK", HttpStatus.OK);
    }
}

