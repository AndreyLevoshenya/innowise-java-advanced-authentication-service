package com.inno.service.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshDtoRequest(@NotBlank String refreshToken) {}
