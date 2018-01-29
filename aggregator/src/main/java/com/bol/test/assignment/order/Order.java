package com.bol.test.assignment.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Order {
    @Getter
    private final int id;

    @Getter
    private final int offerId;

    @Getter
    private final int productId;
}
