package com.supermarket.repository;

import com.supermarket.entity.PurchaseOrder;
import com.supermarket.entity.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
    boolean existsByPoNumber(String poNumber);
}
