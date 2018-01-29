package com.bol.test.assignment.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Product {

    @Getter
    private final int id;

    @Getter
    private final String title;
}
