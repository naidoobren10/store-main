package com.example.store.mapper;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "orderIds", source = "orders")
    ProductDTO productToProductDTO(Product product);

    List<ProductDTO> productsToProductDTOs(List<Product> product);

    default List<Long> map(List<Order> orders) {
        return orders.stream()
                .map(order -> order.getId())
                .distinct()
                .toList();
    }
}
