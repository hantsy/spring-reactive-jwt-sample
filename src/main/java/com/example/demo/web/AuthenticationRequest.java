package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
