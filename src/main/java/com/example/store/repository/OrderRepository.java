package com.example.store.repository;

import com.example.store.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Override
    @NonNull @EntityGraph(attributePaths = {"customer", "products"})
    List<Order> findAll();

    @Override
    @NonNull @EntityGraph(attributePaths = "customer")
    Page<Order> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull @EntityGraph(attributePaths = {"customer", "products"})
    Optional<Order> findById(@NonNull Long id);
}
