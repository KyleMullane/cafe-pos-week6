package com.cafepos;
import com.cafepos.checkout.FixedRateTaxPolicy;
import com.cafepos.checkout.PricingService;
import com.cafepos.checkout.ReceiptPrinter;
import com.cafepos.payment.PaymentStrategy;
import com.cafepos.payment.WalletPayment;
import com.cafepos.smells.OrderManagerGod;
import org.junit.jupiter.api.Test;

import static com.cafepos.smells.OrderManagerGod.process;
import static org.junit.jupiter.api.Assertions.*;

public class Week6CharacterizationTests {
    @Test void no_discount_cash_payment() {
        String receipt = process("ESP+SHOT+OAT", 1,
                "CASH", "NONE", false);
        assertTrue(receipt.startsWith("Order (ESP+SHOT+OAT) x1"));
        assertTrue(receipt.contains("Subtotal: 3.80"));
        assertTrue(receipt.contains("Tax (10%): 0.38"));
        assertTrue(receipt.contains("Total: 4.18"));
    }
    @Test void loyalty_discount_card_payment() {
        String receipt = process("LAT+L", 2, "CARD",
                "LOYAL5", false);
// Latte (Large) base = 3.20 + 0.70 = 3.90, qty 2 => 7.80
// 5% discount => 0.39, discounted=7.41; tax 10% => 0.74;
        // total=8.15;
        assertTrue(receipt.contains("Subtotal: 7.80"));
        assertTrue(receipt.contains("Discount: -0.39"));
        assertTrue(receipt.contains("Tax (10%): 0.74"));
        assertTrue(receipt.contains("Total: 8.15"));
    }
    @Test void coupon_fixed_amount_and_qty_clamp() {
        String receipt = process("ESP+SHOT", 0, "WALLET",
                "COUPON1", false);
// qty=0 clamped to 1; Espresso+SHOT = 2.50 + 0.80 = 3.30;
        // coupon1 => -1 => 2.30; tax=0.23; total=2.53;
        assertTrue(receipt.contains("Order (ESP+SHOT) x1"));
        assertTrue(receipt.contains("Subtotal: 3.30"));
        assertTrue(receipt.contains("Discount: -1.00"));
        assertTrue(receipt.contains("Tax (10%): 0.23"));
        assertTrue(receipt.contains("Total: 2.53"));
    }
    @Test
    void testReceiptPrintingExtraction() {
        // Setup: Create ReceiptPrinter and initiate process
        ReceiptPrinter printer = new ReceiptPrinter();
        String output = printer.formatString("Coffee", 2, new PricingService.PricingResult(subtotal, discount, tax, total), new FixedRateTaxPolicy(10));
        assertNotNull(output);
        assertTrue(output.contains("Order: Coffee x2"));
        // Confirm output contains formatted receipt info
    }

    @Test
    void testPaymentStrategyInjection() {
        PaymentStrategy strategy = new WalletPayment();
        String receipt = new OrderManagerGod(..., strategy).process("Tea", 1, "WALLET", null, false);
        assertTrue(receipt.contains("Total")); // or other relevant assertion
    }

    @Test
    void testConstructorInjection() {
        OrderManagerGod om = new OrderManagerGod(factory, discountPolicy, taxPolicy, printer, paymentStrategy);
        String receipt = om.process("Sandwich", 1, "CARD", null, false);
        assertNotNull(receipt);
    }

    @Test
    void testGlobalStateRemoval() {
        // Verify static variables no longer exist or are used
        assertThrows(NoSuchFieldException.class, () -> {
            OrderManagerGod.class.getDeclaredField("LAST_DISCOUNT_CODE");
        });
    }


}