package com.kiyotakeshi.tdd.products.service;

import com.kiyotakeshi.tdd.products.entity.Product;
import com.kiyotakeshi.tdd.products.repository.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private static final Logger LOGGER = LogManager.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        LOGGER.info("Saving new product with name:{}", product.getName());
        product.setVersion(1);
        return productRepository.save(product);
    }

    public Product update(Product product) {
        LOGGER.info("Updating product with id:{}", product.getId());
        Product existingProduct = productRepository.findProductById(product.getId());
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setQuantity(product.getQuantity());
            existingProduct = productRepository.save(existingProduct);
        } else {
            LOGGER.error("Product with id {} could not be updated!", product.getId());
        }
        return existingProduct;
    }

    public Product findById(Integer id) {
        LOGGER.info("Finding product by id:{}", id);
        return productRepository.findProductById(id);
    }

    public void delete(Integer id) {
        LOGGER.info("Deleting product with id:{}", id);
        Product existingProduct = productRepository.findProductById(id);
        if (existingProduct != null) {
            productRepository.delete(existingProduct);
        } else {
            LOGGER.error("Product with id {} could not be found!", id);
        }
    }

    public Iterable<Product> findAll() {
        return productRepository.findAll();
    }
}
