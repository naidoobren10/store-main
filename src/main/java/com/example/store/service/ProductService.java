package com.example.store.service;

import com.example.store.dto.CreateProductRequestDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.error.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductDTO createProduct(CreateProductRequestDTO createProductRequestDTO) {
        Product product = new Product();
        product.setDescription(createProductRequestDTO.getDescription());
        return productMapper.productToProductDTO(productRepository.save(product));
    }

    public List<ProductDTO> getAllProducts() {
        return productMapper.productsToProductDTOs(productRepository.findAll());
    }

    public ProductDTO getProductById(Long id) {
        return productMapper.productToProductDTO(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id)));
    }

}
