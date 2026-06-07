package com.example.store.service;

import com.example.store.dto.CreateProductRequestDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.error.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductDTO createProduct(CreateProductRequestDTO createProductRequestDTO) {
        log.info("Creating product with description='{}'", createProductRequestDTO.getDescription());
        Product product = new Product();
        product.setDescription(createProductRequestDTO.getDescription());
        ProductDTO productDTO = productMapper.productToProductDTO(productRepository.save(product));
        log.info("Created product with id={}", productDTO.getId());
        return productDTO;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        List<ProductDTO> productDTOs = productMapper.productsToProductDTOs(productRepository.findAll());
        log.info("Fetched {} products", productDTOs.size());
        return productDTOs;
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product by id={}", id);
        ProductDTO productDTO = productMapper.productToProductDTO(productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found for id={}", id);
                    return new ProductNotFoundException("Product not found: " + id);
                }));
        log.info("Fetched product id={}", productDTO.getId());
        return productDTO;
    }

}
