package com.supermarket.service;

import com.supermarket.entity.Product;
import com.supermarket.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.search(keyword);
    }

    public Product findByScanCode(String code) {
        if (code == null) {
            return null;
        }
        final String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return productRepository.findByBarcode(trimmed)
                .or(() -> productRepository.findBySku(trimmed))
                .orElse(null);
    }

    public List<Product> lowStock() {
        return productRepository.findAll().stream().filter(Product::isLowStock).toList();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    public void save(Product product) {
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void adjustStock(Long id, int delta) {
        Product product = findById(id);
        int newStock = product.getStock() + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        product.setStock(newStock);
        productRepository.save(product);
    }
}
