package com.kiyotakeshi.tdd.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiyotakeshi.tdd.products.entity.Product;
import com.kiyotakeshi.tdd.products.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProductsControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test product found - GET /products/1")
    void testGetProductByIdFindsProduct() throws Exception {
        var product = new Product(1, "hamburger", "yummy, but height calories", 2, 1);

        // mocked service method
        doReturn(product).when(productService).findById(product.getId());

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
    @DisplayName("Test all product found - GET /products")
    void testAllProductsFound() throws Exception {
        var product1 = new Product(1, "hamburger", "yummy, but height calories", 2, 1);
        var product2 = new Product(2, "gyoza", "yummy, and you can take vegetable", 3, 1);

        List<Product> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        doReturn(products).when(productService).findAll();

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].name", is("hamburger")))
                .andExpect(jsonPath("$[1].name", is("gyoza")));
    }

    @Test
    @DisplayName("Test add a new product - POST /products")
    void testAddNewProduct() throws Exception {
        var product = new Product("new product", "new product description", 5);
        var mockProduct = new Product(1, "new product", "new product description", 5, 1);

        doReturn(mockProduct).when(productService).save(ArgumentMatchers.any());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("new product")))
                .andExpect(jsonPath("$.description", is("new product description")))
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("Test update an existing product - PUT /products/1")
    void testUpdatingProduct() throws Exception {
        var product = new Product("new product", "new product description", 9);
        var mockProduct = new Product(1, "mock product", "mock product description", 4, 1);

        doReturn(mockProduct).when(productService).findById(1);
        doReturn(mockProduct).when(productService).update(ArgumentMatchers.any());

        mockMvc.perform(put("/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(new ObjectMapper().writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("new product")))
                .andExpect(jsonPath("$.description", is("new product description")))
                .andExpect(jsonPath("$.quantity", is(9)))
                .andExpect(jsonPath("$.version", is(2)));
    }

    @Test
    @DisplayName("Test version mismatch while updating an existing product - PUT /products/1")
    void testVersionMismatchWhileUpdating() throws Exception {
        var product = new Product("new product", "new product description", 9);
        var mockProduct = new Product(1, "mock product", "mock product description", 4, 2);

        doReturn(mockProduct).when(productService).findById(1);

        mockMvc.perform(put("/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(new ObjectMapper().writeValueAsString(product)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Product not found while updating - PUT /products/1")
    public void testProductNotFoundWhileUpdating() throws Exception{
        // Prepare mock product
        var product = new Product("New name", "New description", 20);

        // Prepare mock service method
        doReturn(null).when(productService).findById(1);

        // Perform PUT request
        mockMvc.perform(put("/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(new ObjectMapper().writeValueAsString(product)))

                // Validate 404 NOT_FOUND received
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete a product successfully - DELETE /products/1")
    public void testProductDeletedSuccessfully() throws Exception {
        // Prepare mock product
        var product = new Product(1, "New name", "New description", 20, 1);

        // Prepare mock service method
        doReturn(product).when(productService).findById(1);

        // Perform DELETE request
        mockMvc.perform(delete("/products/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Fail to delete an non-existing product - DELETE /products/1")
    public void testFailureToDeleteNonExistingProduct() throws Exception {
        // Prepare mock service method
        doReturn(null).when(productService).findById(1);

        // Perform DELETE request
        mockMvc.perform(delete("/products/{id}", 1))
                .andExpect(status().isNotFound());
    }
}