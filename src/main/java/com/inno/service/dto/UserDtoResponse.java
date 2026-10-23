package com.inno.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDtoResponse(Long id, String name, String surname, LocalDate birthDate, String email, Boolean active,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
}
