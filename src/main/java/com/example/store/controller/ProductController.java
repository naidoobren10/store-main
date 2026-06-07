package com.example.store.controller;

import com.example.store.dto.CreateProductRequestDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.service.ProductService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequestDTO request) {
        log.info("Received create product request for description='{}'", request.getDescription());
        ProductDTO productDTO = productService.createProduct(request);
        log.info("Returning created product with id={}", productDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("Received get all products request");
        List<ProductDTO> products = productService.getAllProducts();
        log.info("Returning {} products", products.size());
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        log.info("Received get product request for id={}", productId);
        ProductDTO productDTO = productService.getProductById(productId);
        log.info("Returning product with id={}", productDTO.getId());
        return ResponseEntity.status(HttpStatus.OK).body(productDTO);
    }
}
