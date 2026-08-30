package com.supermarket.service;

import com.supermarket.entity.Product;
import com.supermarket.entity.Sale;
import com.supermarket.entity.SaleItem;
import com.supermarket.entity.User;
import com.supermarket.repository.ProductRepository;
import com.supermarket.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public SaleService(SaleRepository saleRepository,
                       ProductRepository productRepository,
                       ProductService productService) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Sale findById(Long id) {
        return saleRepository.findById(id).orElseThrow();
    }

    public String nextReceiptNumber() {
        String base = "RCP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String candidate = base;
        int counter = 0;
        while (saleRepository.existsByReceiptNumber(candidate)) {
            candidate = base + "-" + (++counter);
        }
        return candidate;
    }

    @Transactional
    public Sale checkout(List<Long> productIds, List<Integer> quantities, User cashier,
                         String customerName, BigDecimal discount, BigDecimal taxRate) {
        Sale sale = new Sale();
        sale.setReceiptNumber(nextReceiptNumber());
        sale.setCashier(cashier);
        sale.setSaleDate(LocalDateTime.now());
        sale.setCustomerName(customerName);
        sale.setDiscount(discount == null ? BigDecimal.ZERO : discount);
        sale.setTaxRate(taxRate == null ? BigDecimal.ZERO : taxRate);

        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Product product = productService.findById(productIds.get(i));
            int qty = quantities.get(i);
            if (qty <= 0) {
                continue;
            }
            if (product.getStock() < qty) {
                throw new IllegalArgumentException(
                        "Insufficient stock for " + product.getName() + " (only " + product.getStock() + " available)");
            }
            product.setStock(product.getStock() - qty);
            productRepository.save(product);
            items.add(new SaleItem(product, qty, product.getPrice()));
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No items in cart");
        }
        sale.setItems(items);

        BigDecimal subtotal = sale.getSubtotal();
        BigDecimal discountVal = sale.getDiscount();
        BigDecimal taxableBase = subtotal.subtract(discountVal);
        if (taxableBase.compareTo(BigDecimal.ZERO) < 0) {
            taxableBase = BigDecimal.ZERO;
        }
        BigDecimal taxAmt = taxableBase
                .multiply(sale.getTaxRate())
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        sale.setTaxAmount(taxAmt);
        return saleRepository.save(sale);
    }
}
