package com.cafepos;
import com.cafepos.checkout.*;
import com.cafepos.common.Money;
import com.cafepos.factory.ProductFactory;
import com.cafepos.payment.PaymentStrategy;
import com.cafepos.payment.WalletPayment;
import com.cafepos.smells.OrderManagerGod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Week6CharacterizationTests {
    private ProductFactory factory;
    private DiscountPolicy discountPolicy;
    private TaxPolicy taxPolicy;
    private ReceiptPrinter printer;
    private PaymentStrategy paymentStrategy;
    private OrderManagerGod orderManager;


    @BeforeEach
    void setUp() {
        factory = new ProductFactory();
        discountPolicy = DiscountPolicy.fromCode(null); // Default to NoDiscount
        taxPolicy = new FixedRateTaxPolicy(10);
        printer = new ReceiptPrinter();
        paymentStrategy = new WalletPayment(); // Default for tests
        orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
    }



    @Test
    void no_discount_cash_payment() {
        String receipt = orderManager.process("ESP+SHOT+OAT", 1,
                "CASH", "NONE", false);
        assertTrue(receipt.startsWith("Order (ESP+SHOT+OAT) x1"));
        assertTrue(receipt.contains("Subtotal: 3.80"));
        assertTrue(receipt.contains("Tax (10%): 0.38"));
        assertTrue(receipt.contains("Total: 4.18"));
    }
    @Test
    void loyalty_discount_card_payment() {
        // Latte (Large) base = 3.20 + 0.70 = 3.90, qty 2 => 7.80
        // 5% discount => 0.39, discounted=7.41; tax 10% => 0.74; total=8.15;
        discountPolicy = DiscountPolicy.fromCode("LOYAL5");
        orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
        String receipt = orderManager.process("LAT+L", 2, "CARD", "LOYAL5", false);
        assertTrue(receipt.contains("Subtotal: 7.8"));
        assertTrue(receipt.contains("Discount: -0.39"));
        assertTrue(receipt.contains("Tax (10%): 0.74"));
        assertTrue(receipt.contains("Total: 8.15"));
    }
    @Test
    void coupon_fixed_amount_and_qty_clamp() {
        // ESP+SHOT base price 2.50 + 0.80 = 3.30, qty 0 clamped to 1,
        // coupon -1.00, tax 0.23, total 2.53
        discountPolicy = DiscountPolicy.fromCode("COUPON1");
        orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
        String receipt = orderManager.process("ESP+SHOT", 0, "WALLET", "COUPON1", false);
        assertTrue(receipt.contains("Order (ESP+SHOT) x1"));
        assertTrue(receipt.contains("Subtotal: 3.30"));
        assertTrue(receipt.contains("Discount: -1.00"));
        assertTrue(receipt.contains("Tax (10%): 0.23"));
        assertTrue(receipt.contains("Total: 2.53"));
    }
    @Test
    void testReceiptPrintingExtraction() {
        Money subtotal = Money.of(5.00);
        Money discount = Money.of(0.50);
        Money tax = Money.of(0.45);
        Money total = Money.of(5.45);
        PricingService.PricingResult pr = new PricingService.PricingResult(subtotal, discount, tax, total);
        String output = printer.format("Coffee", 2, pr, taxPolicy);
        assertNotNull(output);
        assertTrue(output.contains("Order (Coffee) x2"));
        assertTrue(output.contains("Subtotal: 5.00"));
        assertTrue(output.contains("Discount: -0.50"));
        assertTrue(output.contains("Tax (10%): 0.45"));
        assertTrue(output.contains("Total: 5.45"));
    }

    @Test
    void testPaymentStrategyInjection() {
        PaymentStrategy strategy = new WalletPayment();;
        orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
        String receipt = orderManager.process("Tea", 1, "WALLET", null, false);
        assertTrue(receipt.contains("Total"));
        assertTrue(System.out.toString().contains("[Wallet] Customer paid")); // or other relevant assertion
    }

    @Test
    void testConstructorInjection() {
        ProductFactory Factory = new ProductFactory();
        DiscountPolicy Discount = DiscountPolicy.fromCode(null);
        TaxPolicy Tax = new FixedRateTaxPolicy(10);
        ReceiptPrinter Printer = new ReceiptPrinter();
        PaymentStrategy Strategy = new WalletPayment();

        OrderManagerGod om = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
        String receipt = om.process("Sandwich", 1, "CARD", null, false);
        assertNotNull(receipt);
    }

    @Test
    void testGlobalStateRemoval() {
        assertThrows(NoSuchFieldException.class, () -> {
            OrderManagerGod.class.getDeclaredField("TAX_PERCENT");
        });

        assertThrows(NoSuchFieldException.class, () -> {
            OrderManagerGod.class.getDeclaredField("LAST_DISCOUNT_CODE");
        });
    }

    @Nested
    class DiscountPolicyTest {

        @Test
        void loyalty_discount_5_percent() {
            DiscountPolicy d = new LoyaltyPercentDiscount(5);
            assertEquals(Money.of(0.39), d.discountOf(Money.of(7.80)));
        }
    }

    @Nested
    class TaxPolicyTest {

        @Test
        void fixed_rate_tax_10_percent() {
            TaxPolicy t = new FixedRateTaxPolicy(10);
            assertEquals(Money.of(0.74), t.taxOn(Money.of(7.41)));
        }
    }

    @Nested
    class PricingServiceTest {

        @Test
        void pricing_pipeline() {
            var pricing = new PricingService(new LoyaltyPercentDiscount(5), new FixedRateTaxPolicy(10));
            var pr = pricing.price(Money.of(7.80));

            assertEquals(Money.of(0.39), pr.discount());
            assertEquals(Money.of(7.41), Money.of(pr.subtotal().asBigDecimal()
                    .subtract(pr.discount().asBigDecimal())));
            assertEquals(Money.of(0.74), pr.tax());
            assertEquals(Money.of(8.15), pr.total());
        }
    }

}