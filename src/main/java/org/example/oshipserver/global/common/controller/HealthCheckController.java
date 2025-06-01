package org.example.oshipserver.global.common.controller;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private final String uuid = String.valueOf(UUID.randomUUID());

    @GetMapping("/health")
    public String healthCheck() {
        return "OK-" + uuid;
    }
}