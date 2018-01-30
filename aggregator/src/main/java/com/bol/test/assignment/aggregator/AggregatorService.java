package com.bol.test.assignment.aggregator;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bol.test.assignment.aggregator.EnrichedOrder.EnrichedOrderBuilder;
import com.bol.test.assignment.offer.Offer;
import com.bol.test.assignment.offer.OfferCondition;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.Order;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.Product;
import com.bol.test.assignment.product.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AggregatorService {
    // the timeout could be parameterizable, instead this constant
    private static final long DEFAULT_TIMEOUT = 2000; // miliseconds
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final OrderService orderService;
    private final OfferService offerService;
    private final ProductService productService;

    /**
     * Instantiates a new {@link AggregatorService} service.
     *
     * @param orderService
     *            the {@link OrderService} service
     * @param offerService
     *            the {@link OfferService} service
     * @param productService
     *            the {@link ProductService} service
     */
    public AggregatorService(final OrderService orderService, final OfferService offerService, final ProductService productService) {
        this.orderService = orderService;
        this.offerService = offerService;
        this.productService = productService;
    }

    /**
     * Enrich the offer with information from {@link Offer} and {@link Product}.
     *
     * @param sellerId
     *            the seller id used to retrieve the {@link Order} containing the @link Offer} and {@link Product}.
     * @return the enriched order
     * @throws ExecutionException
     *             the execution exception
     * @throws InterruptedException
     *             the interrupted exception
     */
    public EnrichedOrder enrich(final int sellerId) throws ExecutionException, InterruptedException {
        log.info("Enrich order with seller id: {}", sellerId);
        final EnrichedOrderBuilder builder = EnrichedOrder.builder();

        try {
            final Order order = retrieveOrder(sellerId);
            final Map.Entry<Offer, Product> offerProductEntry = retrieveOfferAndProduct(order);

            enrichData(builder, order, offerProductEntry);
        } catch (final TimeoutException e) {
            throw new RuntimeException("Order retrieval failed with timeout " + DEFAULT_TIMEOUT + " " + TimeUnit.MILLISECONDS + ".", e);
        } catch (final ExecutionException e) {
            throw new RuntimeException("Order retrieval failed.", e);
        }

        final EnrichedOrder enrichedOrder = builder.build();
        log.info("Enriched order with id: {}, offer id: {}, offer condition: {}, product id: {}, product title: {}", new Object[] { enrichedOrder.getId(),
                enrichedOrder.getOfferId(), enrichedOrder.getOfferCondition(), enrichedOrder.getProductId(), enrichedOrder.getProductTitle() });

        return enrichedOrder;
    }

    /**
     * Retrieve order from the {@link OrderService} within a timeout.
     *
     * @param sellerId
     *            the id of the seller used to retrieve the order.
     * @return the {@link Order} order.
     * @throws InterruptedException
     *             the interrupted exception
     * @throws ExecutionException
     *             the execution exception
     * @throws TimeoutException
     *             the timeout exception
     */
    private Order retrieveOrder(final int sellerId) throws InterruptedException, ExecutionException, TimeoutException {
        final Order order = executorService.submit(() -> {
            return orderService.getOrder(sellerId);
        }).get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        return order;
    }

    /**
     * Retrieve offer and product from the {@link OfferService} and {@link ProductService} within a timeout.
     * First submit both the offer and product {@link Callable}, only after fetch the data.
     *
     * @param order
     *            the {@link Order} order containing offer id and product id.
     * @return the pair of {@link Offer} and {@link Product}
     */
    private Map.Entry<Offer, Product> retrieveOfferAndProduct(final Order order) {
        final Future<Offer> offerFuture = executorService.submit(() -> {
            return offerService.getOffer(order.getOfferId());
        });

        final Future<Product> productFuture = executorService.submit(() -> {
            return productService.getProduct(order.getProductId());
        });

        final Offer offer = fetchOffer(offerFuture);
        final Product product = fetchProduct(productFuture);

        // The pair is needed as a data structure to group the offer and product.
        // Grouping is needed since is the result of one method.
        return new AbstractMap.SimpleImmutableEntry<>(offer, product);
    }

    /**
     * Fetch the offer.
     *
     * @param offerFuture
     *            the {@link Future} offer.
     * @return the {@link Offer} offer.
     */
    private Offer fetchOffer(final Future<Offer> offerFuture) {
        Offer offer = null;
        try {
            offer = offerFuture.get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            log.info("Retrieved offer with id: {} and in condition {}", offer.getId(), offer.getCondition());
        } catch (final Exception e) {
            log.error("Cannot retrieve offer.", e);
        }
        return offer;
    }

    /**
     * Fetch the product.
     *
     * @param productFuture
     *            the {@link Future} product.
     * @return the {@link Product} product.
     */
    private Product fetchProduct(final Future<Product> productFuture) {
        Product product = null;
        try {
            product = productFuture.get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            log.info("Retrieved product with id: {} and title {}", product.getId(), product.getTitle());
        } catch (final Exception e) {
            log.error("Cannot retrieve product.", e);
        }
        return product;
    }

    /**
     * Enrich the order with information from the {@link Order}, {@link Offer} and {@link Product}.
     * This methods only delegates the addition of the new data to the builder.
     *
     * @param builder
     *            the {@link EnrichedOrderBuilder} builder.
     * @param order
     *            the {@link Order} order
     * @param offerProductEntry
     *            the offer product entry
     */
    private void enrichData(final EnrichedOrderBuilder builder, final Order order, final Entry<Offer, Product> offerProductEntry) {
        addOrderData(builder, order);
        addOfferData(builder, offerProductEntry.getKey());
        addProductData(builder, offerProductEntry.getValue());
    }

    /**
     * Adds the order data.
     *
     * @param builder
     *            the {@link EnrichedOrderBuilder} builder.
     * @param order
     *            the {@link Order} order
     */
    private void addOrderData(final EnrichedOrderBuilder builder, final Order order) {
        builder.id(order.getId());
    }

    /**
     * Adds the product information to the builder {@link EnrichedOrderBuilder}.
     * In case the product is null, sets -1 product id and the title to null.
     *
     * @param builder
     *            the builder
     * @param product
     *            the {@link Product} product
     */
    private void addProductData(final EnrichedOrderBuilder builder, final Product product) {
        if (product != null) {
            builder.productId(product.getId()).productTitle(product.getTitle());
        } else {
            builder.productId(-1).productTitle(null);
        }
    }

    /**
     * Adds the offer information to the builder {@link EnrichedOrderBuilder}.
     * In case the offer is null, sets -1 the offer id and OfferCondition.UNKNOWN the offer condition.
     *
     * @param builder
     *            the {@link EnrichedOrderBuilder} builder.
     * @param offer
     *            the {@link Offer} offer.
     */
    private void addOfferData(final EnrichedOrderBuilder builder, final Offer offer) {
        if (offer != null) {
            builder.offerId(offer.getId()).offerCondition(offer.getCondition());
        } else {
            builder.offerId(-1).offerCondition(OfferCondition.UNKNOWN);
        }
    }
}
