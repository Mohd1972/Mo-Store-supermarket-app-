package com.supermarket.controller;

import com.supermarket.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String report(@RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model) {
        LocalDate end = (to != null) ? to : LocalDate.now();
        LocalDate start = (from != null) ? from : end.minusDays(30);
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("totalRevenue", reportService.totalRevenueBetween(startTime, endTime));
        model.addAttribute("totalSales", reportService.salesCountBetween(startTime, endTime));
        model.addAttribute("salesPerDay", reportService.salesPerDay(startTime, endTime));
        model.addAttribute("topProducts", reportService.topProducts(10));
        return "reports/report";
    }
}
