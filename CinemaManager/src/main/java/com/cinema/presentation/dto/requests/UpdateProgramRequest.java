package com.cinema.presentation.dto.requests;

import java.time.LocalDate;

public record UpdateProgramRequest(String name, String description, LocalDate startDate, LocalDate endDate) {
}
