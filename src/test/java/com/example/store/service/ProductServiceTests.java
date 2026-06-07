package com.example.store.service;

import com.example.store.dto.CreateProductRequestDTO;
import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.error.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private CreateProductRequestDTO request;
    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        request = new CreateProductRequestDTO();
        request.setDescription("Test Product");

        product = new Product();
        product.setId(1L);
        product.setDescription(request.getDescription());

        productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setDescription(product.getDescription());
        productDTO.setOrderIds(List.of(10L, 20L));
    }

    @Test
    void createProductSavesMappedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.createProduct(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product productToSave = productCaptor.getValue();
        assertEquals("Test Product", productToSave.getDescription());
        assertSame(productDTO, result);
    }

    @Test
    void getAllProductsReturnsMappedProducts() {
        List<Product> products = List.of(product);
        List<ProductDTO> productDTOs = List.of(productDTO);

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.productsToProductDTOs(products)).thenReturn(productDTOs);

        List<ProductDTO> result = productService.getAllProducts();

        assertSame(productDTOs, result);
    }

    @Test
    void getProductByIdReturnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.productToProductDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.getProductById(1L);

        assertSame(productDTO, result);
    }

    @Test
    void getProductByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(1L));

        assertEquals("Product not found: 1", exception.getMessage());
        verify(productRepository).findById(1L);
    }
}
