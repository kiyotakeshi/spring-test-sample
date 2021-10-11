package com.kiyotakeshi.tdd.products.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiyotakeshi.tdd.products.entity.Product;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private static File DATA_JSON = Paths.get("src", "test", "resources", "products.json").toFile();

    @BeforeEach
    void setUp() throws IOException {
        Product[] products = new ObjectMapper().readValue(DATA_JSON, Product[].class);
        Arrays.stream(products).forEach(productRepository::save);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Test product with id retrieved successfully")
    void testProductWithIdRetrievedSuccessfully() {
        // Given two products in the database
        var product = productRepository.findProductById(1);
        assertNotNull(product, "product with id 1 should exist");
        assertEquals("First Product", product.getName());
    }

    @Test
    @DisplayName("Test product not found with non-existing id")
    public void testProductNotFoundForNonExistingId(){
        // Given two products in the database

        // When
        var product = productRepository.findProductById(100);

        // Then
        assertNull(product, "Product with id 100 should not exist");
    }

    @Test
    @DisplayName("Test product saved successfully")
    public void testProductSavedSuccessfully(){
        // Prepare mock product
        var newProduct = new Product("New Product", "New Product Description", 8);

        // When
        var savedProduct = productRepository.save(newProduct);

        // Then
        assertNotNull(savedProduct, "Product should be saved");
        assertNotNull(savedProduct.getId(), "Product should have an id when saved");
        assertEquals(newProduct.getName(), savedProduct.getName());
    }

    @Test
    @DisplayName("Test product updated successfully")
    public void testProductUpdatedSuccessfully(){
        // Prepare the product
        var productToUpdate = new Product(1, "Updated Product", "New Product Description", 20, 2);

        // When
        var updatedProduct = productRepository.save(productToUpdate);

        // Then
        assertEquals(productToUpdate.getName(), updatedProduct.getName());
        assertEquals(2, updatedProduct.getVersion());
        assertEquals(20, updatedProduct.getQuantity());
    }

    @Test
    @DisplayName("Test product deleted successfully")
    public void testProductDeletedSuccessfully(){
        // Given two products in the database

        // When
        productRepository.deleteById(1);

        // Then
        assertEquals(1L, productRepository.count());
    }
}