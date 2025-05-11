package org.example.logic;

import org.example.model.Order;
import org.example.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentOptimizerTest {

    @Test
    void testSimpleFullDiscountWithBestMethod() {
        var orders = List.of(order("O1", 100.00, "CARD1", "CARD2"));
        var methods = List.of(
                method("CARD1", 5, 100.00),
                method("CARD2", 10, 100.00),
                method("PUNKTY", 15, 0.00)
        );

        var result = new PaymentOptimizer(orders, methods).optimizePayments();
        assertBD("CARD2", "90.00", result);
    }

    @Test
    void testFullPaymentUsingPointsPreferred() {
        var orders = List.of(order("O1", 100.00, "CARD1"));
        var methods = List.of(
                method("CARD1", 10, 100.00),
                method("PUNKTY", 20, 100.00)
        );

        var result = new PaymentOptimizer(orders, methods).optimizePayments();
        assertBD("PUNKTY", "80.00", result);
    }

    @Test
    void testPartialPointsWithFallback() {
        var orders = List.of(order("O1", 100.00));
        var methods = List.of(
                method("CARD1", 10, 100.00),
                method("PUNKTY", 15, 50.00)
        );

        var result = new PaymentOptimizer(orders, methods).optimizePayments();
        assertBD("PUNKTY", "50.00", result);
        assertBD("CARD1", "40.00", result);
    }

    @Test
    void testNotEnoughPointsOrCard() {
        var orders = List.of(order("O1", 100.00));
        var methods = List.of(
                method("CARD1", 10, 30.00),
                method("PUNKTY", 15, 5.00)
        );

        var result = new PaymentOptimizer(orders, methods).optimizePayments();
        assertTrue(result.isEmpty());
    }

    @Test
    void testNoPromotionsPointsUsedIfEnough() {
        var orders = List.of(order("O1", 80.00));
        var methods = List.of(
                method("CARD1", 10, 100.00),
                method("PUNKTY", 15, 80.00)
        );

        var result = new PaymentOptimizer(orders, methods).optimizePayments();
        assertBD("PUNKTY", "68.00", result);
    }

    @Test
    void testExact10PercentPointsMixed() {
        var orders = List.of(order("O1", 100.00));
        var methods = List.of(
                method("CARD1", 0, 100.00),
                method("PUNKTY", 15, 10.00)
        );

        var result = new PaymentOptimizer(orders, methods).optimizePayments();
        assertBD("PUNKTY", "10.00", result);
        assertBD("CARD1", "80.00", result);
    }

    // ======================== HELPER METHODS ========================

    private static Order order(String id, double value, String... promotions) {
        return new Order(id, BigDecimal.valueOf(value),
                promotions.length == 0 ? null : List.of(promotions));
    }

    private static PaymentMethod method(String id, int discountPercent, double limit) {
        return new PaymentMethod(id, BigInteger.valueOf(discountPercent), BigDecimal.valueOf(limit));
    }

    private static void assertBD(String key, String expected, Map<String, BigDecimal> result) {
        BigDecimal actual = result.get(key);
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                "Expected " + expected + " for " + key + ", but got " + actual);
    }
}