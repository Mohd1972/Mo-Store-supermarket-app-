package com.supermarket.service;

import com.supermarket.entity.Product;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.entity.PurchaseOrderItem;
import com.supermarket.entity.PurchaseOrderStatus;
import com.supermarket.entity.Supplier;
import com.supermarket.repository.ProductRepository;
import com.supermarket.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                ProductRepository productRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
    }

    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id).orElseThrow();
    }

    public String nextPoNumber() {
        String base = "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String candidate = base;
        int counter = 0;
        while (purchaseOrderRepository.existsByPoNumber(candidate)) {
            candidate = base + "-" + (++counter);
        }
        return candidate;
    }

    @Transactional
    public PurchaseOrder create(Supplier supplier, List<Product> products,
                                List<Integer> quantities, List<BigDecimal> unitCosts,
                                LocalDate expectedDeliveryDate, String notes) {
        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setPoNumber(nextPoNumber());
        po.setStatus(PurchaseOrderStatus.PENDING);
        po.setExpectedDeliveryDate(expectedDeliveryDate);
        po.setNotes(notes);

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int qty = quantities.get(i);
            BigDecimal cost = unitCosts.get(i);
            if (qty > 0 && p != null) {
                items.add(new PurchaseOrderItem(p, qty, cost));
            }
        }
        po.setItems(items);
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public void receive(Long id) {
        PurchaseOrder po = findById(id);
        if (po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be received");
        }
        for (PurchaseOrderItem item : po.getItems()) {
            Product p = item.getProduct();
            p.setStock(p.getStock() + item.getQuantity());
            p.setCost(item.getUnitCost());
            p.setPrice(p.getPrice().max(item.getUnitCost().multiply(BigDecimal.valueOf(1.3))));
            productRepository.save(p);
        }
        po.setStatus(PurchaseOrderStatus.RECEIVED);
        po.setReceivedDate(LocalDateTime.now());
        purchaseOrderRepository.save(po);
    }

    @Transactional
    public void cancel(Long id) {
        PurchaseOrder po = findById(id);
        if (po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(po);
    }
}
