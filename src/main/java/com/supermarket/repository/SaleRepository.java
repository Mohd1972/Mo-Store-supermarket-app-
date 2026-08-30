package com.supermarket.repository;

import com.supermarket.entity.Sale;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByReceiptNumber(String receiptNumber);

    List<Sale> findBySaleDateBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT FUNCTION('DATE', s.saleDate), COUNT(s), " +
            "COALESCE(SUM(si.unitPrice * si.quantity), 0) " +
            "FROM SaleItem si JOIN si.sale s " +
            "WHERE s.saleDate BETWEEN :from AND :to " +
            "GROUP BY FUNCTION('DATE', s.saleDate) ORDER BY FUNCTION('DATE', s.saleDate)")
    List<Object[]> salesPerDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT si.product.name, SUM(si.quantity), COALESCE(SUM(si.unitPrice * si.quantity), 0) " +
            "FROM SaleItem si JOIN si.sale s " +
            "WHERE s.saleDate BETWEEN :from AND :to " +
            "GROUP BY si.product.name ORDER BY SUM(si.quantity) DESC")
    List<Object[]> topProducts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("SELECT COALESCE(SUM(si.unitPrice * si.quantity), 0) " +
            "FROM SaleItem si JOIN si.sale s WHERE s.saleDate BETWEEN :from AND :to")
    BigDecimal totalRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(DISTINCT s) FROM SaleItem si JOIN si.sale s WHERE s.saleDate BETWEEN :from AND :to")
    long countBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
