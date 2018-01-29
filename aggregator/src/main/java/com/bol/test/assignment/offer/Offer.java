package com.bol.test.assignment.offer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Offer {
    @Getter
    private final int id;

    @Getter
    private final OfferCondition condition;
}
