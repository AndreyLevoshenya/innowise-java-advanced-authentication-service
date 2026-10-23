package com.inno.service.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDtoRequest(@NotBlank String username, @NotBlank String password) {}
