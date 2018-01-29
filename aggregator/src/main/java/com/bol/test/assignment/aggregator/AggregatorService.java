package com.bol.test.assignment.aggregator;

import java.util.concurrent.ExecutionException;

import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AggregatorService {
    public AggregatorService(final OrderService orderService, final OfferService offerService, final ProductService productService) {
    }

    public EnrichedOrder enrich(final int sellerId) throws ExecutionException, InterruptedException {
        log.info("Enrich order with seller Id: {}", sellerId);
        return null;
    }
}
