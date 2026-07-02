package com.priyanshu.ratelimiter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Rate Limiter Test Controller", description = "Endpoints for testing the rate limiter service")
public class TestController {

    @GetMapping("/hello")
    @Operation(
            summary = "A simple welcome API",
            description = "Returns a welcome string. Subject to rate limits.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully allowed request"),
                    @ApiResponse(responseCode = "429", description = "Rate limit exceeded (Too Many Requests)")
            }
    )
    public String hello() {
        return "Request Allowed! Welcome to the rate limited API.";
    }

    @GetMapping("/products")
    @Operation(
            summary = "Mock products API",
            description = "Simulates fetching product data. Subject to rate limits.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully allowed request"),
                    @ApiResponse(responseCode = "429", description = "Rate limit exceeded (Too Many Requests)")
            }
    )
    public String products() {
        return "Product List: [Product A, Product B, Product C]";
    }

    @GetMapping("/payment")
    @Operation(
            summary = "Mock payments API",
            description = "Simulates processing a payment. Subject to rate limits.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully allowed request"),
                    @ApiResponse(responseCode = "429", description = "Rate limit exceeded (Too Many Requests)")
            }
    )
    public String payment() {
        return "Payment processed successfully!";
    }
}