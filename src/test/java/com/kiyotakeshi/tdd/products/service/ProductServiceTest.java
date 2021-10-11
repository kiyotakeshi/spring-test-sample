package com.kiyotakeshi.tdd.products.service;

import com.kiyotakeshi.tdd.products.entity.Product;
import com.kiyotakeshi.tdd.products.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    @Test
    @DisplayName("Find product with id successfully")
    void testFindProductById() {
        var product = new Product(1, "mock product", "mock product description", 5, 1);

        doReturn(product).when(productRepository).findProductById(1);

        var foundProduct = productService.findById(1);

        assertNotNull(foundProduct);
        assertSame("mock product", foundProduct.getName());
        assertSame(5, foundProduct.getQuantity());
    }

    @Test
    @DisplayName("Fail to find product with id")
    void testFailToFindProductById() {
        doReturn(null).when(productRepository).findProductById(1);
        var foundProduct = productService.findById(1);
        assertNull(foundProduct);
    }

    @Test
    @DisplayName("Find all products")
    void testFindAllProducts() {
        var product1 = new Product(1, "mock product1", "mock product description1", 5, 1);
        var product2 = new Product(2, "mock product2", "mock product description2", 6, 1);

        doReturn(Arrays.asList(product1, product2)).when(productRepository).findAll();

        Iterable<Product> products = productService.findAll();

        assertEquals(2, ((Collection<?>) (products)).size());
    }

    @Test
    @DisplayName("Save new products successfully")
    void testSuccessfulProductSave() {
        var product = new Product(1, "mock product", "mock product description", 5, 1);
        doReturn(product).when(productRepository).save(any());

        Product saveProduct = productService.save(product);
        // import static org.springframework.test.util.AssertionErrors.*;
        // assertNotNull("c", saveProduct);
        assertNotNull(saveProduct);
        assertSame("mock product", product.getName());
    }

    @Test
    @DisplayName("Update an existing product successfully")
    void testSuccessfulUpdateProduct() {
        var product = new Product(1, "mock product", "mock product description", 5, 1);
        var updatedProduct = new Product(1, "updated product", "updated product description", 9, 2);

        doReturn(product).when(productRepository).findProductById(1);
        doReturn(updatedProduct).when(productRepository).save(product);

        Product updateProduct = productService.update(product);

        assertEquals("updated product",updatedProduct.getName());
        assertEquals(2,updatedProduct.getVersion());
    }

    @Test
    @DisplayName("Fail to update an existing product")
    void testFailToUpdateExistingProduct() {
        var product = new Product(1, "mock product", "mock product description", 5, 1);
        doReturn(null).when(productRepository).findProductById(1);
        Product updateProduct = productService.update(product);
        assertNull(updateProduct, "Product shouldn't be null");
    }
}