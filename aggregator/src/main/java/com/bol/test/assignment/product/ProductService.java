package com.bol.test.assignment.product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductService {
    public Product getProduct(final int id) {
        final String productTitle = "Product title";
        log.info("New Product with id {} and title {}", id, productTitle);

        return new Product(id, productTitle);
    }
}
