package com.cafepos.demo;

import java.util.*;
import com.cafepos.catalog.*;
import com.cafepos.common.Money;
import com.cafepos.decorator.*;
import com.cafepos.factory.ProductFactory;
import com.cafepos.payment.*;
import com.cafepos.checkout.*;
import com.cafepos.smells.OrderManagerGod;

public final class Week5Demo {
    // A simple structure for breakdown, adjust if implementing differently
    static class ItemPrice {
        final String name;
        final Money price;
        public ItemPrice(String n, Money p) { name = n; price = p; }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ProductFactory factory = new ProductFactory();

        // Menu prompt with prices
        System.out.println("Select base drink:");
        System.out.println("1) Espresso (€2.50)");
        System.out.println("2) Cappuccino (€3.00)");
        System.out.println("3) Latte    (€3.20)");
        int type = scanner.nextInt(); scanner.nextLine();
        String baseCode = switch (type) {
            case 1 -> "ESP";
            case 2 -> "CAP";
            case 3 -> "LAT";
            default -> throw new IllegalArgumentException("Bad input");
        };

        // Prepare breakdown to show decorators and their individual prices
        StringBuilder recipe = new StringBuilder(baseCode);
        List<ItemPrice> itemized = new ArrayList<>();

        // Add base first
        Product baseProduct = factory.create(baseCode);
        itemized.add(new ItemPrice(baseProduct.name(), baseProduct.basePrice()));

        boolean addingDecorators = true;
        while (addingDecorators) {
            System.out.println("Add a decorator?");
            System.out.println("1) Extra Shot (€0.80)");
            System.out.println("2) Syrup      (€0.40)");
            System.out.println("3) Oat Milk   (€0.50)");
            System.out.println("4) Size Large (€0.70)");
            System.out.println("5) No more decorators");
            int choice = scanner.nextInt(); scanner.nextLine();
            switch (choice) {
                case 1 -> {
                    recipe.append("+SHOT");
                    itemized.add(new ItemPrice("Extra Shot", Money.of(0.80)));
                    System.out.println("Added Extra Shot (+€0.80)");
                }
                case 2 -> {
                    recipe.append("+SYP");
                    itemized.add(new ItemPrice("Syrup", Money.of(0.40)));
                    System.out.println("Added Syrup (+€0.40)");
                }
                case 3 -> {
                    recipe.append("+OAT");
                    itemized.add(new ItemPrice("Oat Milk", Money.of(0.50)));
                    System.out.println("Added Oat Milk (+€0.50)");
                }
                case 4 -> {
                    recipe.append("+L");
                    itemized.add(new ItemPrice("Large Size", Money.of(0.70)));
                    System.out.println("Added Size Large (+€0.70)");
                }
                case 5 -> addingDecorators = false;
                default -> System.out.println("Invalid choice");
            }
        }
        System.out.print("Enter quantity: ");
        int qty = scanner.nextInt(); scanner.nextLine();

        // Discount/Coupon
        System.out.println("Select discount/coupon:");
        System.out.println("1) No Discount");
        System.out.println("2) 5% Loyalty (LOYAL5)");
        System.out.println("3) €1.00 Coupon (COUPON1)");
        int discountOption = scanner.nextInt(); scanner.nextLine();
        String discountCode = switch (discountOption) {
            case 1 -> "NONE";
            case 2 -> "LOYAL5";
            case 3 -> "COUPON1";
            default -> "NONE";
        };

        // Payment type
        System.out.println("Select payment type:");
        System.out.println("1) Cash");
        System.out.println("2) Card");
        System.out.println("3) Wallet");
        int paymentOption = scanner.nextInt(); scanner.nextLine();
        String paymentType = switch (paymentOption) {
            case 1 -> "CASH";
            case 2 -> "CARD";
            case 3 -> "WALLET";
            default -> "CASH";
        };

        // Receipt printer object for both flows
        ReceiptPrinter printer = new ReceiptPrinter();

        // --- OLD FLOW (using OrderManagerGod) ---
        DiscountPolicy oldDP = DiscountPolicy.fromCode(discountCode);
        TaxPolicy oldTP = new FixedRateTaxPolicy(10);
        PaymentStrategy oldPS =
                paymentType.equals("CASH") ? new CashPayment() :
                        paymentType.equals("CARD") ? new CardPayment() : new WalletPayment();
        OrderManagerGod omg = new OrderManagerGod(factory, oldDP, oldTP, printer, oldPS);
        String oldReceipt = omg.process(recipe.toString(), qty, paymentType, discountCode, false);

        // --- NEW FLOW (using PricingService and ReceiptPrinter) ---
        // Compose product using ProductFactory
        Product product = factory.create(recipe.toString());
        Money unitPrice = itemized.stream().map(i -> i.price).reduce(Money.zero(), Money::add);
        Money subtotal = unitPrice.multiply(qty);

        DiscountPolicy newDP = DiscountPolicy.fromCode(discountCode);
        TaxPolicy newTP = new FixedRateTaxPolicy(10);
        Money discount = newDP.discountOf(subtotal);
        Money discounted = subtotal.subtract(discount);
        Money tax = newTP.taxOn(discounted);
        Money total = discounted.add(tax);

        PricingService.PricingResult pr = new PricingService.PricingResult(subtotal, discount, tax, total);
        String newReceipt = printer.format(product.name(), qty, pr, newTP);

        // Print both receipts for comparison
        System.out.println("\n===== OLD RECEIPT (smelly) =====");
        System.out.println(oldReceipt);
        System.out.println("\n===== NEW RECEIPT (clean) ======");
        for (ItemPrice ip : itemized) {
            System.out.printf("%-18s %8s\n", ip.name, "€" + ip.price);
        }
        System.out.printf("%-18s %8s\n", "x Quantity", qty);
        System.out.println("------------------------------");
        System.out.printf("%-18s %8s\n", "SUBTOTAL:", "€" + subtotal);
        System.out.printf("%-18s %8s\n", "DISCOUNT:", "€" + discount);
        System.out.printf("%-18s %8s\n", "TAX (10%):", "€" + tax);
        System.out.printf("%-18s %8s\n", "TOTAL:", "€" + total);
        System.out.println("======================================");

        // Show proof: Text blocks are identical
        System.out.println("\n===== Proof: text blocks identical? =====");
        System.out.println(oldReceipt.equals(newReceipt)
                ? "YES! The receipts are identical."
                : "NO! The receipts are NOT identical.");
    }
}
