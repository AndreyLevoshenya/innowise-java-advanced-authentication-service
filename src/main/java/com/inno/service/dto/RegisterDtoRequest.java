package com.inno.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterDtoRequest(@NotBlank @Size(min = 3, max = 50) String username,
                                 @NotBlank @Size(min = 8, max = 100) String password,
                                 @NotBlank(message = "Name must not be blank") @Size(max = 100, message = "Name must not exceed 100 characters") String name,
                                 @NotBlank(message = "Surname must not be blank") @Size(max = 100, message = "Surname must not exceed 100 characters") String surname,
                                 @NotNull(message = "Birth date is required") @Past(message = "Birth date must be in the past") LocalDate birthDate,
                                 @NotBlank(message = "Email must not be blank") @Email(message = "Email must be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,
                                 @NotNull(message = "Active status is required") Boolean active) {
}
