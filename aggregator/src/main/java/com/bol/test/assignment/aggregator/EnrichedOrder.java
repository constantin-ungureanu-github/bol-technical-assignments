package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.OfferCondition;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@lombok.Builder
public class EnrichedOrder {
    @Getter
    private final int id;

    @Getter
    private final int offerId;

    @Getter
    private final OfferCondition offerCondition;

    @Getter
    private final int productId;

    @Getter
    private final String productTitle;
}
