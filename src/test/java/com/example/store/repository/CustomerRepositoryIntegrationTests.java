package com.example.store.repository;

import com.example.store.entity.Customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("store-test")
            .withUsername("admin")
            .withPassword("admin");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> true);
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("delete from \"order\"");
        jdbcTemplate.execute("delete from customer");

        customerRepository.save(customer("Joe Doe"));
        customerRepository.save(customer("John Deer"));
        customerRepository.save(customer("Banjo Kane"));
        customerRepository.save(customer("Jade Doe"));
        customerRepository.save(customer("Anne-Marie Jones"));
        customerRepository.save(customer("O'Connor Shaun"));
    }

    @Test
    void findByNameContainingQueryStringMatchesOrderedWordPrefixes() {
        List<Customer> results =
                customerRepository.findByNameContainingQueryString("^jo[^[:space:]]*\\s+d[^[:space:]]*(\\s+.*)?$");

        assertEquals(
                List.of("Joe Doe", "John Deer"),
                results.stream().map(Customer::getName).sorted().toList());
    }

    @Test
    void findByNameContainingQueryStringReturnsEmptyListWhenNothingMatches() {
        List<Customer> results = customerRepository.findByNameContainingQueryString("^xx[^[:space:]]*(\\s+.*)?$");

        assertTrue(results.isEmpty());
    }

    @Test
    void findByNameContainingQueryStringSupportsPunctuationInsideNameWords() {
        List<Customer> hyphenResults =
                customerRepository.findByNameContainingQueryString("^anne-m[^[:space:]]*(\\s+.*)?$");
        List<Customer> apostropheResults =
                customerRepository.findByNameContainingQueryString("^o'c[^[:space:]]*(\\s+.*)?$");

        assertEquals(
                List.of("Anne-Marie Jones"),
                hyphenResults.stream().map(Customer::getName).toList());
        assertEquals(
                List.of("O'Connor Shaun"),
                apostropheResults.stream().map(Customer::getName).toList());
    }

    private static Customer customer(String name) {
        Customer customer = new Customer();
        customer.setName(name);
        return customer;
    }
}
