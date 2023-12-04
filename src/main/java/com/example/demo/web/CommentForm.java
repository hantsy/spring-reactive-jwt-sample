package com.example.demo.web;

import jakarta.validation.constraints.NotBlank;

public record CommentForm(
        @NotBlank
        String content

) {
}
