package com.supermarket.controller;

import com.supermarket.service.ProductService;
import com.supermarket.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ReportService reportService;
    private final ProductService productService;

    public DashboardController(ReportService reportService, ProductService productService) {
        this.reportService = reportService;
        this.productService = productService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", reportService.dashboard());
        model.addAttribute("lowStockProducts", productService.lowStock());
        return "dashboard";
    }
}
