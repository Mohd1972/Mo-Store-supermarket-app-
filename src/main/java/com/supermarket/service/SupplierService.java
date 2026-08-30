package com.supermarket.service;

import com.supermarket.entity.Supplier;
import com.supermarket.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id).orElseThrow();
    }

    public void save(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        supplierRepository.deleteById(id);
    }
}
