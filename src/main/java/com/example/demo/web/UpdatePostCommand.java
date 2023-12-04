package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostCommand(
        @NotBlank String title,
        @NotBlank String content
) {
}
