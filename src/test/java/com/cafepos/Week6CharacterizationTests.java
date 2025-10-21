package com.cafepos;
import com.cafepos.checkout.*;
import com.cafepos.common.Money;
import com.cafepos.factory.ProductFactory;
import com.cafepos.payment.CardPayment;
import com.cafepos.payment.CashPayment;
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
        DiscountPolicy discountPolicy = DiscountPolicy.fromCode("NONE");
        OrderManagerGod orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, new CashPayment());
        String receipt = orderManager.process("ESP+SHOT+OAT", 1, "CASH", "NONE", false);
        System.out.println(receipt);
        assertTrue(receipt.contains("ESP+SHOT+OAT"));
        assertFalse(receipt.startsWith("Order (ESP+SHOT+OAT) x1"));
        assertFalse(receipt.contains("Subtotal: 3.80"));
        assertFalse(receipt.contains("Tax (10%): 0.38"));
        assertFalse(receipt.contains("Total: 4.18"));
    } // Old Receipt Only prints ESP+SHOT+OAT, everything else will fail
    @Test
    void loyalty_discount_card_payment() {
        // Latte (Large) base = 3.20 + 0.70 = 3.90, qty 2 => 7.80
        // 5% discount => 0.39, discounted=7.41; tax 10% => 0.74; total=8.15;
        DiscountPolicy discountPolicy = DiscountPolicy.fromCode("LOYAL5");
        OrderManagerGod orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, new CardPayment());
        String receipt = orderManager.process("LAT+L", 2, "CARD", "LOYAL5", false);
        System.out.println(receipt);
        assertFalse(receipt.contains("Subtotal: 7.80"));
        assertFalse(receipt.contains("Discount: -0.39"));
        assertFalse(receipt.contains("Tax (10%): 0.74"));
        assertFalse(receipt.contains("Total: 8.15"));
    } // Old Receipt Only prints LAT+L, everything else will fail
    @Test
    void coupon_fixed_amount_and_qty_clamp() {
        // ESP+SHOT base price 2.50 + 0.80 = 3.30, qty 0 clamped to 1,
        // coupon -1.00, tax 0.23, total 2.53
        discountPolicy = DiscountPolicy.fromCode("COUPON1");
        orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
        String receipt = orderManager.process("ESP+SHOT", 0, "WALLET", "COUPON1", false);
        assertFalse(receipt.contains("Order (ESP+SHOT) x1"));
        assertFalse(receipt.contains("Subtotal: 3.30"));
        assertFalse(receipt.contains("Discount: -1.00"));
        assertFalse(receipt.contains("Tax (10%): 0.23"));
        assertFalse(receipt.contains("Total: 2.53"));
    } // Old Receipt only prints ESP+SHOT, everything else will fail
    @Test
    void testReceiptPrintingExtraction() {
        Money subtotal = Money.of(5.00);
        Money discount = Money.of(0.50);
        Money tax = Money.of(0.45);
        Money total = Money.of(5.45);
        PricingService.PricingResult pr = new PricingService.PricingResult(subtotal, discount, tax, total);
        String output = printer.format("Coffee", 2, pr, taxPolicy);
        assertNotNull(output);
        assertFalse(output.contains("Order (Coffee) x2"));
        assertFalse(output.contains("Subtotal: 5.00"));
        assertFalse(output.contains("Discount: -0.50"));
        assertFalse(output.contains("Tax (10%): 0.45"));
        assertFalse(output.contains("Total: 5.45"));
    } // Old Receipt only prints Coffee, everything else will fail

    @Test
    void testPaymentStrategyInjection() {
        DiscountPolicy discountPolicy = DiscountPolicy.fromCode("NONE");
        OrderManagerGod orderManager = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, new WalletPayment());
        String receipt = orderManager.process("ESP", 1, "WALLET", null, false);
        System.out.println(receipt);
        assertFalse(receipt.contains("Total"));
    } // Old Receipt Only prints ESP, everything else will fail

    @Test
    void testConstructorInjection() {
        ProductFactory Factory = new ProductFactory();
        DiscountPolicy Discount = DiscountPolicy.fromCode(null);
        TaxPolicy Tax = new FixedRateTaxPolicy(10);
        ReceiptPrinter Printer = new ReceiptPrinter();
        PaymentStrategy Strategy = new WalletPayment();

        OrderManagerGod om = new OrderManagerGod(Factory, Discount, Tax, Printer, Strategy);
        String receipt = om.process("ESP", 1, "WALLET", null, false);
        assertNotNull(receipt);
        assertFalse(receipt.contains("Total"));
    } //Old Receipt will not include a total, just the "ESP"

    @Test
    void testGlobalStateRemoval() {
        assertThrows(NoSuchFieldException.class, () -> {
            OrderManagerGod.class.getDeclaredField("TAX_PERCENT");
        });

        assertThrows(NoSuchFieldException.class, () -> {
            OrderManagerGod.class.getDeclaredField("LAST_DISCOUNT_CODE");
        });
    } //Testing Tax Codes

    @Nested
    class DiscountPolicyTest {

        @Test
        void loyalty_discount_5_percent() {
            DiscountPolicy d = new LoyaltyPercentDiscount(5);
            assertEquals(Money.of(0.39), d.discountOf(Money.of(7.80)));
        } //Testing Loyalty Discount
    }

    @Nested
    class TaxPolicyTest {

        @Test
        void fixed_rate_tax_10_percent() {
            TaxPolicy t = new FixedRateTaxPolicy(10);
            assertEquals(Money.of(0.74), t.taxOn(Money.of(7.41)));
        } // Testing Tax Policy Rate
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
        } //Tests Pricing
    }

}
