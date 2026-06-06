package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;


public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = "orders")
    List<Customer> findAll();

    @Query(value = """
    select *
    from customer c
    where lower(c.name) ~ :pattern
    """, nativeQuery = true)
    List<Customer> findByNameContainingQueryString(@Param("pattern") String pattern);
}
