package com.bol.test.assignment.product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductService {
    final String productTitle = "Product title";

    /**
     * Gets the {@link Product} product.
     *
     * @param id
     *            the id of the {@link Product} product
     * @return the product
     */
    public Product getProduct(final int id) {
        log.info("New Product with id {} and title {}", id, productTitle);

        return new Product(id, productTitle);
    }
}
