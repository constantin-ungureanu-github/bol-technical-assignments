package com.bol.test.assignment.offer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OfferService {
    final OfferCondition offerCondition = OfferCondition.AS_NEW;

    /**
     * Gets the {@link Offer} offer.
     *
     * @param id
     *            the offer id
     * @return the {@link Offer} offer
     */
    public Offer getOffer(final int id) {
        log.info("New Offer with id: {}, condition: ", id, offerCondition);

        return new Offer(id, offerCondition);
    }
}
