package com.priyanshu.ratelimiter.controller;

import com.priyanshu.ratelimiter.service.RateLimiterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final RateLimiterService service;

    public TestController(RateLimiterService service) {
        this.service = service;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String user) {

        if (service.allow(user)) {

            return "Request Allowed";

        }

        return "Too Many Requests";

    }

}