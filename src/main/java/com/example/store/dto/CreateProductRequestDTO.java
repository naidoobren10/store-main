package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CreateProductRequestDTO {

    @NotBlank(message = "description is required")
    private String description;
}
