package com.cinema.presentation.dto.requests;

import java.time.LocalDate;

public record CreateProgramRequest(String name, String description, LocalDate startDate, LocalDate endDate) {
}
