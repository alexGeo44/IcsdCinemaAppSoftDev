package com.cinema.presentation.dto.responses;

public record AuthResponse(String token, UserResponse user) {
}
