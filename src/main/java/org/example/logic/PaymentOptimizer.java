package org.example.logic;

import org.example.model.Order;
import org.example.model.PaymentMethod;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaymentOptimizer {

    private static final BigDecimal TEN_PERCENT = new BigDecimal("0.1");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final String PUNKTY = "PUNKTY";

    private final List<Order> orders;
    private final Map<String, PaymentMethod> methodMap;
    private final Map<String, BigDecimal> usage = new HashMap<>();

    public PaymentOptimizer(List<Order> orders, List<PaymentMethod> methods) {
        this.orders = Objects.requireNonNull(orders);
        this.methodMap = methods.stream()
                .collect(Collectors.toMap(PaymentMethod::getId, Function.identity()));
    }

    public Map<String, BigDecimal> optimizePayments() {
        for (Order order : orders) {
            if (!tryApplyBestFullPayment(order)) {
                tryApplyMixedPointsPayment(order);
            }
        }
        return usage;
    }

    // ========== FULL DISCOUNT LOGIC ==========

    private boolean tryApplyBestFullPayment(Order order) {
        PaymentCandidate best = findBestFullPaymentCandidate(order);
        if (best != null) {
            applyPayment(best.methodId(), best.amountToPay());
            return true;
        }
        return false;
    }

    private PaymentCandidate findBestFullPaymentCandidate(Order order) {
        BigDecimal orderValue = order.getValue();
        BigDecimal bestDiscount = BigDecimal.ZERO;
        String bestMethod = null;

        for (String methodId : getPromotions(order)) {
            if (canFullyPayWith(methodId, orderValue)) {
                BigDecimal discount = calcDiscount(orderValue, methodMap.get(methodId).getDiscount());
                if (discount.compareTo(bestDiscount) > 0) {
                    bestDiscount = discount;
                    bestMethod = methodId;
                }
            }
        }

        if (canFullyPayWith(PUNKTY, orderValue)) {
            BigDecimal discount = calcDiscount(orderValue, methodMap.get(PUNKTY).getDiscount());
            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                bestMethod = PUNKTY;
            }
        }

        return bestMethod != null ? new PaymentCandidate(bestMethod, orderValue.subtract(bestDiscount)) : null;
    }

    // ========== MIXED (PARTIAL POINTS) LOGIC ==========

    private void tryApplyMixedPointsPayment(Order order) {
        if (!canApplyPartialPoints(order)) return;

        BigDecimal value = order.getValue();
        BigDecimal tenPercent = calcTenPercent(value);
        BigDecimal toPay = value.subtract(tenPercent);

        BigDecimal availablePoints = methodMap.get(PUNKTY).getLimit();
        BigDecimal pointsToUse = toPay.min(availablePoints);

        if (pointsToUse.compareTo(tenPercent) < 0) return;
        BigDecimal remaining = toPay.subtract(pointsToUse);

        String fallbackMethod = findMethodToCoverRemainder(remaining);
        if (fallbackMethod != null) {
            applyPayment(PUNKTY, pointsToUse);
            applyPayment(fallbackMethod, remaining);
        }
    }

    private boolean canApplyPartialPoints(Order order) {
        PaymentMethod points = methodMap.get(PUNKTY);
        if (points == null) return false;
        BigDecimal value = order.getValue();
        return points.getLimit().compareTo(calcTenPercent(value)) >= 0;
    }

    private String findMethodToCoverRemainder(BigDecimal remainder) {
        return methodMap.values().stream()
                .filter(pm -> !PUNKTY.equals(pm.getId()))
                .filter(pm -> pm.getLimit().compareTo(remainder) >= 0)
                .map(PaymentMethod::getId)
                .findFirst()
                .orElse(null);
    }

    // ========== HELPER METHODS ==========

    private boolean canFullyPayWith(String methodId, BigDecimal amount) {
        PaymentMethod method = methodMap.get(methodId);
        return method != null && method.getLimit().compareTo(amount) >= 0;
    }

    private List<String> getPromotions(Order order) {
        return order.getPromotions() != null ? order.getPromotions() : List.of();
    }

    private BigDecimal calcDiscount(BigDecimal value, BigInteger percent) {
        return value.multiply(new BigDecimal(percent)).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcTenPercent(BigDecimal value) {
        return value.multiply(TEN_PERCENT).setScale(2, RoundingMode.HALF_UP);
    }

    private void applyPayment(String methodId, BigDecimal amount) {
        PaymentMethod method = methodMap.get(methodId);
        BigDecimal before = method.getLimit();
        BigDecimal after = before.subtract(amount);
        method.setLimit(after);
        usage.merge(methodId, amount, BigDecimal::add);
    }

    private record PaymentCandidate(String methodId, BigDecimal amountToPay) { }

}
