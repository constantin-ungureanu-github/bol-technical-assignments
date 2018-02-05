package com.bol.test.assignment.offer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OfferService {
    final OfferCondition offerCondition = OfferCondition.AS_NEW;

    public Offer getOffer(final int id) {
        log.info("New Offer with id: {}, condition: ", id, offerCondition);

        return new Offer(id, offerCondition);
    }
}
