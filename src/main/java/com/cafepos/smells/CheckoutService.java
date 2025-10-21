package com.cafepos.smells;

import com.cafepos.catalog.Product;
import com.cafepos.checkout.PricingService;
import com.cafepos.checkout.ReceiptPrinter;
import com.cafepos.common.Money;
import com.cafepos.decorator.Priced;
import com.cafepos.factory.ProductFactory;
import com.cafepos.payment.PaymentStrategy;

public final class CheckoutService {
    private final ProductFactory factory;
    private final PricingService pricing;
    private final ReceiptPrinter printer;
    private final PaymentStrategy paymentStrategy;
    private final int taxPercent;

    public CheckoutService(ProductFactory factory, PricingService pricing, ReceiptPrinter printer,
                           PaymentStrategy paymentStrategy, int taxPercent) {
        this.factory = factory;
        this.pricing = pricing;
        this.printer = printer;
        this.paymentStrategy = paymentStrategy;
        this.taxPercent = taxPercent;
    }

    public String checkout(String recipe, int qty) {
        Product product = factory.create(recipe);
        if (qty <= 0) qty = 1;

        Money unit = (product instanceof Priced p)
                ? p.price()
                : product.basePrice();
        Money subtotal = unit.multiply(qty);

        var pricingResult = pricing.price(subtotal);


        Money Money = null;
        paymentStrategy.process(Money);

        return printer.formatString(recipe, qty, pricingResult, taxPercent);
    }
}
