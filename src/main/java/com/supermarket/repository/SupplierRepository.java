package com.supermarket.repository;

import com.supermarket.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByCompanyName(String companyName);
    java.util.Optional<Supplier> findByCompanyName(String companyName);
}
