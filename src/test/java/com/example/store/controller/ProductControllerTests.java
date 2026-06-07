package com.example.store.controller;

import com.example.store.dto.CreateProductRequestDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.error.ProductNotFoundException;
import com.example.store.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductDTO productDTO;
    private CreateProductRequestDTO createProductRequestDTO;

    @BeforeEach
    void setUp() {
        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setDescription("Test Product");
        productDTO.setOrderIds(List.of(10L, 20L));

        createProductRequestDTO = new CreateProductRequestDTO();
        createProductRequestDTO.setDescription("Test Product");
    }

    @Test
    void createProduct() throws Exception {
        when(productService.createProduct(any(CreateProductRequestDTO.class))).thenReturn(productDTO);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProductRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Test Product"))
                .andExpect(jsonPath("$.orderIds[0]").value(10))
                .andExpect(jsonPath("$.orderIds[1]").value(20));
    }

    @Test
    void createProductWhenDescriptionMissing() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void createProductWhenDescriptionBlank() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("One or more request fields are invalid."));
    }

    @Test
    void getAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDTO));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test Product"))
                .andExpect(jsonPath("$[0].orderIds[0]").value(10))
                .andExpect(jsonPath("$[0].orderIds[1]").value(20));
    }

    @Test
    void getProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Product"))
                .andExpect(jsonPath("$.orderIds[0]").value(10))
                .andExpect(jsonPath("$.orderIds[1]").value(20));
    }

    @Test
    void getProductByIdWhenProductNotFound() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException("Product not found: 1"));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.detail").value("The product could not be found."));
    }
}
