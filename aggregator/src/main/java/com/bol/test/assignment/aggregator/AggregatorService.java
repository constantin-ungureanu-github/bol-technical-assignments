package com.bol.test.assignment.aggregator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bol.test.assignment.aggregator.EnrichedOrder.EnrichedOrderBuilder;
import com.bol.test.assignment.offer.OfferCondition;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AggregatorService {
    private static final long TIMEOUT = 2000; // miliseconds

    private final OrderService orderService;
    private final OfferService offerService;
    private final ProductService productService;

    public AggregatorService(final OrderService orderService, final OfferService offerService, final ProductService productService) {
        this.orderService = orderService;
        this.offerService = offerService;
        this.productService = productService;
    }

    public EnrichedOrder enrich(final int sellerId) throws ExecutionException, InterruptedException {
        log.info("Enrich order with seller id: {}", sellerId);
        final EnrichedOrderBuilder builder = EnrichedOrder.builder();

        try {
            CompletableFuture.supplyAsync(() -> orderService.getOrder(sellerId)).thenCompose(order -> CompletableFuture.runAsync(() -> {
                builder.id(order.getId());
                builder.offerId(-1).offerCondition(OfferCondition.UNKNOWN);
                builder.productId(-1).productTitle(null);

                final CompletableFuture<Void> offerFuture = CompletableFuture.supplyAsync(() -> offerService.getOffer(order.getOfferId()))
                        .thenAccept(offer -> builder.offerId(offer.getId()).offerCondition(offer.getCondition()));

                final CompletableFuture<Void> productFuture = CompletableFuture.supplyAsync(() -> productService.getProduct(order.getProductId()))
                        .thenAccept(product -> builder.productId(product.getId()).productTitle(product.getTitle()));

                try {
                    CompletableFuture.allOf(offerFuture, productFuture).get(TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                }
            })).get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            throw new RuntimeException("Order retrieval failed with timeout " + TIMEOUT + " " + TimeUnit.MILLISECONDS + ".", e);
        } catch (final ExecutionException e) {
            throw new RuntimeException("Order retrieval failed.", e);
        }

        final EnrichedOrder enrichedOrder = builder.build();
        log.info("Enriched order with id: {}, offer id: {}, offer condition: {}, product id: {}, product title: {}", new Object[] { enrichedOrder.getId(),
                enrichedOrder.getOfferId(), enrichedOrder.getOfferCondition(), enrichedOrder.getProductId(), enrichedOrder.getProductTitle() });

        return enrichedOrder;
    }
}
