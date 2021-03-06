package com.bol.test.assignment.aggregator;

import static com.bol.test.assignment.offer.OfferCondition.AS_NEW;
import static com.bol.test.assignment.offer.OfferCondition.UNKNOWN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import com.bol.test.assignment.offer.Offer;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.Order;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.Product;
import com.bol.test.assignment.product.ProductService;

public class AggregatorServiceTest {
    private final OrderService orderService = mock(OrderService.class);
    private final OfferService offerService = mock(OfferService.class);
    private final ProductService productService = mock(ProductService.class);
    private final AggregatorService aggregatorService = new AggregatorService(orderService, offerService, productService);
    private final int sellerId = 1;
    private final int orderId = 2;
    private final int offerId = 3;
    private final int productId = 4;
    private final String title = "Title";

    @Test
    public void simpleHappyFlow() throws ExecutionException, InterruptedException {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenReturn(new Offer(offerId, AS_NEW));
        when(productService.getProduct(productId)).thenReturn(new Product(productId, title));

        final EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
    }

    @Test
    public void orderServiceIsSlow() throws Exception {
        when(orderService.getOrder(sellerId)).thenAnswer((final InvocationOnMock invocationOnMock) -> {
            Thread.sleep(1500);
            return new Order(orderId, offerId, productId);
        });
        when(offerService.getOffer(offerId)).thenReturn(new Offer(offerId, AS_NEW));
        when(productService.getProduct(productId)).thenReturn(new Product(productId, title));

        final EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
    }

    @Test(timeout = 2000)
    public void offerAndProductServicesAreSlow() throws InterruptedException, ExecutionException {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenAnswer((final InvocationOnMock invocationOnMock) -> {
            Thread.sleep(1500);
            return new Offer(offerId, AS_NEW);
        });
        when(productService.getProduct(productId)).thenAnswer((final InvocationOnMock invocationOnMock) -> {
            Thread.sleep(1500);
            return new Product(productId, title);
        });

        final EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getOfferCondition(), is(AS_NEW));
        assertThat(enrichedOrder.getProductTitle(), is(title));
    }

    @Test(expected = RuntimeException.class)
    public void orderServiceFailed() throws Exception {
        when(orderService.getOrder(sellerId)).thenThrow(new RuntimeException("Order service failed"));

        aggregatorService.enrich(sellerId);
    }

    @Test
    public void offerServiceFailed() throws Exception {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenThrow(new RuntimeException("Offer Service failed"));
        when(productService.getProduct(productId)).thenReturn(new Product(productId, title));

        final EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertThat(enrichedOrder.getProductTitle(), is(title));
        assertThat(enrichedOrder.getOfferId(), is(-1));
        assertThat(enrichedOrder.getOfferCondition(), is(UNKNOWN));
    }

    @Test
    public void productServiceFailed() throws Exception {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenReturn(new Offer(offerId, AS_NEW));
        when(productService.getProduct(productId)).thenThrow(new RuntimeException("Product Service failed"));

        final EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertNull(enrichedOrder.getProductTitle());
        assertThat(enrichedOrder.getOfferId(), is(offerId));
        assertThat(enrichedOrder.getOfferCondition(), is(AS_NEW));
    }

    @Test
    public void productServiceAndOfferServiceFailed() throws Exception {
        when(orderService.getOrder(sellerId)).thenReturn(new Order(orderId, offerId, productId));
        when(offerService.getOffer(offerId)).thenThrow(new RuntimeException("Offer Service failed"));
        when(productService.getProduct(productId)).thenThrow(new RuntimeException("Product Service failed"));

        final EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);
        assertThat(enrichedOrder.getId(), is(orderId));
        assertNull(enrichedOrder.getProductTitle());
        assertThat(enrichedOrder.getOfferId(), is(-1));
        assertThat(enrichedOrder.getOfferCondition(), is(UNKNOWN));
    }
}
