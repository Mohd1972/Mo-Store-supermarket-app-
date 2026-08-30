package com.supermarket.repository;

import com.supermarket.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByStockLessThanEqual(int reorderLevel);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.reorderLevel")
    List<Product> findAllLowStock();

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> search(@Param("keyword") String keyword);
}
