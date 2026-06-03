package com.example.store.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
public class OrderRequestDTO {

    @NotBlank(message = "A description is required")
    private String description;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
}
