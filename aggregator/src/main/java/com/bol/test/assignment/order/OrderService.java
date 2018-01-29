package com.bol.test.assignment.order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderService {

    /**
     * Retrieve the {@link Order} order.
     *
     * @param sellerId
     *            the id of the seller
     * @return the {@link Order} order
     */
    public Order getOrder(final int sellerId) {
        log.info("New Order with seller id: {}, offer id: {}, product id: {}", sellerId, 2, 3);

        return new Order(sellerId, 2, 3);
    }
}
