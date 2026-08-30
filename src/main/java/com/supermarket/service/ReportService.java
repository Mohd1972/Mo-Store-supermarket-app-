package com.supermarket.service;

import com.supermarket.repository.ProductRepository;
import com.supermarket.repository.SaleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public ReportService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    public Map<String, Object> dashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("todayRevenue", saleRepository.totalRevenueBetween(startOfDay, now));
        model.put("todaySales", saleRepository.countBetween(startOfDay, now));
        model.put("totalProducts", productRepository.count());
        model.put("lowStockCount", (long) productRepository.findAllLowStock().size());

        List<Map<String, Object>> top = topProducts(5);
        model.put("topProducts", top);

        return model;
    }

    public BigDecimal totalRevenueBetween(LocalDateTime from, LocalDateTime to) {
        return saleRepository.totalRevenueBetween(from, to);
    }

    public long salesCountBetween(LocalDateTime from, LocalDateTime to) {
        return saleRepository.countBetween(from, to);
    }

    public List<Map<String, Object>> salesPerDay(LocalDateTime from, LocalDateTime to) {
        return saleRepository.salesPerDay(from, to).stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("date", row[0]);
                    m.put("count", row[1]);
                    m.put("total", row[2]);
                    return m;
                })
                .toList();
    }

    public List<Map<String, Object>> topProducts(int limit) {
        LocalDateTime from = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime to = LocalDateTime.now();
        return saleRepository.topProducts(from, to, PageRequest.of(0, limit)).stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", row[0]);
                    m.put("quantity", row[1]);
                    m.put("revenue", row[2]);
                    return m;
                })
                .toList();
    }
}
