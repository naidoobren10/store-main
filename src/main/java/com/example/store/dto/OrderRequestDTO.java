package com.example.store.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;


@Data
public class OrderRequestDTO {

    @NotBlank(message = "A description is required")
    private String description;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotEmpty(message = "At least one product ID is required")
    private List<Long> productIds;
}
