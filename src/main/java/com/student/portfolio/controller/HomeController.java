package com.student.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> homePage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the Student Portfolio Tracking System API");
        return ResponseEntity.ok(response);
    }
}
