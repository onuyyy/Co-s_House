package com.bird.cos.service.cart;

import com.bird.cos.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {

    @Test
    void lineTotal_roundsToWholeWon() {
        BigDecimal unit = new BigDecimal("12.345678");
        BigDecimal total = PriceCalculator.lineTotal(unit, 3);
        assertThat(total).isEqualTo(new BigDecimal("37"));
    }

    @Test
    void effectiveUnitPrice_picksLowestPositiveWithScale() {
        Product p = product(
                "100.000000", // original
                "99.999999",  // sale
                "100.000001", // coupon
                "5.123456"    // discount rate
        );
        BigDecimal unit = PriceCalculator.effectiveUnitPrice(p);
        // discounted original = 100 * (1 - 0.05123456) = 94.876544 -> scale 0 (HALF_UP) => 95
        assertThat(unit).isEqualTo(new BigDecimal("95"));
    }

    private static Product product(String original, String sale, String coupon, String rate) {
        try {
            Product p = new Product();
            setField(p, "originalPrice", new BigDecimal(original));
            setField(p, "salePrice", new BigDecimal(sale));
            setField(p, "couponPrice", new BigDecimal(coupon));
            setField(p, "discountRate", new BigDecimal(rate));
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
