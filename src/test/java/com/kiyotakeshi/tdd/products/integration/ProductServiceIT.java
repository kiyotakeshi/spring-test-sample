package com.kiyotakeshi.tdd.products.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiyotakeshi.tdd.products.entity.Product;
import com.kiyotakeshi.tdd.products.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductServiceIT {

    @Autowired
    private MockMvc mockMvc;

    // use real service layer
    @Autowired
    private ProductRepository productRepository;

    private final File DATA_JSON = Paths.get("src", "test", "resources", "products.json").toFile();

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
    void testGetProductByIdFindsProduct() throws Exception {

        mockMvc.perform(get("/products/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("hamburger")))
                .andExpect(jsonPath("$.description", is("yummy, but height calories")))
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("Test all products found - GET /products")
    public void testAllProductsFound() throws Exception {
        // Perform GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/products"))
                // Validate 200 OK and JSON response type received
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                // Validate response body
                .andExpect(jsonPath("$[0].name", is("First Product")))
                .andExpect(jsonPath("$[1].name", is("Second Product")));
    }

    @Test
    @DisplayName("Add a new product - POST /products")
    @DirtiesContext
    public void testAddNewProduct() throws Exception {
        // Prepare product to save
        Product newProduct = new Product("New Product", "New Product Description", 8);

        // Perform POST request
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writeValueAsString(newProduct)))

                // Validate 201 CREATED and JSON response type received
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                // Validate response headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/3"))

                // Validate response body
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Product")))
                .andExpect(jsonPath("$.quantity", is(8)))
                .andExpect(jsonPath("$.version", is(1)));
    }
}
