package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(
        @NotBlank String status
) {
}
