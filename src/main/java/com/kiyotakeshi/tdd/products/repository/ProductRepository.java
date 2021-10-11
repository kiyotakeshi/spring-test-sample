package com.kiyotakeshi.tdd.products.repository;

import com.kiyotakeshi.tdd.products.entity.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Integer> {
    Product findProductById(Integer id);
    Product findProductByIdAndName(Integer id, String name);
}
