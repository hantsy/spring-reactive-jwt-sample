package com.example.demo.web;

import java.util.List;

public record PaginatedResult<T>(List<T> data, Long count) {
}
