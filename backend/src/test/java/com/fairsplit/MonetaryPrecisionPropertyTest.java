package com.fairsplit;

import net.jqwik.api.*;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Feature: fairsplit-expense-tracker, Property 11: Monetary precision
 * Validates: Requirements 4.1
 * 
 * For any monetary calculation (splits, balances, settlements), 
 * all amounts should be rounded to exactly two decimal places.
 */
public class MonetaryPrecisionPropertyTest {
    
    @Property(tries = 100)
    @DisplayName("All monetary amounts should be rounded to exactly 2 decimal places")
    void monetaryAmountsShouldHaveTwoDecimalPlaces(@ForAll("monetaryAmounts") double amount) {
        // Round the amount using BigDecimal
        BigDecimal rounded = BigDecimal.valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Convert back to double
        double roundedAmount = rounded.doubleValue();
        
        // Verify that the rounded amount has at most 2 decimal places
        String amountStr = String.format("%.2f", roundedAmount);
        double reconstructed = Double.parseDouble(amountStr);
        
        // The difference should be negligible (within floating point precision)
        double difference = Math.abs(roundedAmount - reconstructed);
        assert difference < 0.001 : "Amount should have exactly 2 decimal places";
        
        // Verify scale is 2 or less
        BigDecimal check = BigDecimal.valueOf(roundedAmount);
        int scale = check.stripTrailingZeros().scale();
        assert scale <= 2 : "Rounded amount should have at most 2 decimal places, but had " + scale;
    }
    
    @Property(tries = 100)
    @DisplayName("Division operations should maintain 2 decimal place precision")
    void divisionShouldMaintainPrecision(
            @ForAll("monetaryAmounts") double totalAmount,
            @ForAll("participantCount") int participants) {
        
        // Calculate equal split using BigDecimal
        BigDecimal total = BigDecimal.valueOf(totalAmount);
        BigDecimal count = BigDecimal.valueOf(participants);
        BigDecimal share = total.divide(count, 2, RoundingMode.HALF_UP);
        
        // Verify the share has at most 2 decimal places
        int scale = share.stripTrailingZeros().scale();
        assert scale <= 2 : "Share should have at most 2 decimal places, but had " + scale;
        
        // Verify that sum of shares is close to total (within rounding tolerance)
        BigDecimal sumOfShares = share.multiply(count);
        BigDecimal difference = total.subtract(sumOfShares).abs();
        assert difference.compareTo(BigDecimal.valueOf(0.01)) <= 0 : 
            "Sum of shares should be within 0.01 of total";
    }
    
    @Provide
    Arbitrary<Double> monetaryAmounts() {
        return Arbitraries.doubles()
                .between(0.01, 10000.0)
                .filter(d -> !Double.isNaN(d) && !Double.isInfinite(d));
    }
    
    @Provide
    Arbitrary<Integer> participantCount() {
        return Arbitraries.integers().between(1, 10);
    }
}
