package com.cafepos.smells;
import com.cafepos.checkout.DiscountPolicy;
import com.cafepos.checkout.FixedRateTaxPolicy;
import com.cafepos.checkout.TaxPolicy;
import com.cafepos.common.Money;
import com.cafepos.factory.ProductFactory;
import com.cafepos.catalog.Product;

public class OrderManagerGod {
    public static int TAX_PERCENT = 10; // Global/Static State
    public static String LAST_DISCOUNT_CODE = null; // Global/Static State

    public static String process(String recipe, int qty, String
            paymentType, String discountCode, boolean printReceipt) {
        // God Class & Long Method: This big method does creation, pricing, discounting, tax,
        // payment I/O, and printing all in one place (responsibility overload).

        ProductFactory factory = new ProductFactory();
        Product product = factory.create(recipe);

        Money unitPrice;
        try {
            var priced = product instanceof com.cafepos.decorator.Priced
                    p ? p.price() : product.basePrice();
            unitPrice = priced;
        } catch (Exception e) {
            unitPrice = product.basePrice();
        }

        if (qty <= 0) qty = 1; // Primitive Obsession: magic number 0 quantity check and handling

        Money subtotal = unitPrice.multiply(qty);
        DiscountPolicy discountPolicy = DiscountPolicy.fromCode(discountCode);
        Money discount = discountPolicy.discountOf(subtotal);


        if (discountCode != null) {
            // Primitive Obsession: discountCode is a String with hardcoded values
            if (discountCode.equalsIgnoreCase("LOYAL5")) {
                // Duplicated Logic & Feature Envy / Shotgun Surgery: inline Money and BigDecimal manipulations;
                // tax/discount rules embedded inline—change requires this method to be edited directly
                discount = Money.of(subtotal.asBigDecimal()
                        .multiply(java.math.BigDecimal.valueOf(5))
                        .divide(java.math.BigDecimal.valueOf(100)));
            } else if (discountCode.equalsIgnoreCase("COUPON1")) {
                discount = Money.of(1.00); // Primitive Obsession: magic number 1.00
            } else if (discountCode.equalsIgnoreCase("NONE")) {
                discount = Money.zero();
            } else {
                discount = Money.zero();
            }
            LAST_DISCOUNT_CODE = discountCode; // Global/Static State: shared mutable state
        }

        Money discounted =
                Money.of(subtotal.asBigDecimal().subtract(discount.asBigDecimal()));
        if (discounted.asBigDecimal().signum() < 0) discounted =
                Money.zero();

//        var tax = Money.of(discounted.asBigDecimal()
//                .multiply(java.math.BigDecimal.valueOf(TAX_PERCENT)) // Primitive Obsession: TAX_PERCENT primitive used here inline
//                .divide(java.math.BigDecimal.valueOf(100))); // Primitive Obsession: magic number 100 for percentage calculation

        TaxPolicy taxPolicy = new FixedRateTaxPolicy(10);
        var tax = taxPolicy.taxOn(discounted);
        var total = discounted.add(tax);

        if (paymentType != null) {
            // Primitive Obsession: paymentType string with hardcoded values
            // Feature Envy / Shotgun Surgery risk: payment handling logic inline here, changes force edits here
            if (paymentType.equalsIgnoreCase("CASH")) {
                System.out.println("[Cash] Customer paid " + total + " EUR");
            } else if (paymentType.equalsIgnoreCase("CARD")) {
                System.out.println("[Card] Customer paid " + total + " EUR with card ****1234");
            } else if (paymentType.equalsIgnoreCase("WALLET")) {
                System.out.println("[Wallet] Customer paid " + total + " EUR via wallet user-wallet-789");
            } else {
                System.out.println("[UnknownPayment] " + total);
            }
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("Order (").append(recipe).append(") x").append(qty).append("\n");
        receipt.append("Subtotal: ").append(subtotal).append("\n");
        if (discount.asBigDecimal().signum() > 0) {
            receipt.append("Discount: -").append(discount).append("\n");
        }
        //receipt.append("Tax (").append(TAX_PERCENT).append("%): ").append(tax).append("\n");
        receipt.append("Tax (").append(taxPolicy.getRatePercent()).append("%): ").append(tax).append("\n");
        receipt.append("Total: ").append(total);

        String out = receipt.toString();

        if (printReceipt) {
            System.out.println(out);
        }

        return out;
    }
}

