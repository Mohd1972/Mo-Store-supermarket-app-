package com.supermarket.controller;

import com.supermarket.entity.Product;
import com.supermarket.entity.PurchaseOrder;
import com.supermarket.entity.PurchaseOrderStatus;
import com.supermarket.service.ProductService;
import com.supermarket.service.PurchaseOrderService;
import com.supermarket.service.SupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final ProductService productService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
                                   SupplierService supplierService,
                                   ProductService productService) {
        this.purchaseOrderService = purchaseOrderService;
        this.supplierService = supplierService;
        this.productService = productService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", purchaseOrderService.findAll());
        return "purchase-orders/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("types", PurchaseOrderStatus.values());
        return "purchase-orders/form";
    }

    @PostMapping
    public String create(@RequestParam Long supplierId,
                         @RequestParam(required = false) List<Long> productId,
                         @RequestParam(required = false) List<Integer> quantity,
                         @RequestParam(required = false) List<BigDecimal> unitCost,
                         @RequestParam(required = false) LocalDate expectedDeliveryDate,
                         @RequestParam(required = false) String notes,
                         RedirectAttributes ra) {
        try {
            List<Product> products = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();
            List<BigDecimal> costs = new ArrayList<>();
            if (productId != null) {
                for (int i = 0; i < productId.size(); i++) {
                    Long pid = productId.get(i);
                    Integer qty = quantity.get(i);
                    if (pid != null && qty != null && qty > 0) {
                        products.add(productService.findById(pid));
                        quantities.add(qty);
                        costs.add(unitCost.get(i));
                    }
                }
            }
            PurchaseOrder po = purchaseOrderService.create(
                    supplierService.findById(supplierId), products, quantities, costs,
                    expectedDeliveryDate, notes);
            ra.addFlashAttribute("success", "Purchase order " + po.getPoNumber() + " created");
            return "redirect:/purchase-orders";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            return "redirect:/purchase-orders/new";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", purchaseOrderService.findById(id));
        return "purchase-orders/detail";
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id, RedirectAttributes ra) {
        try {
            purchaseOrderService.receive(id);
            ra.addFlashAttribute("success", "Order received, stock updated");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            purchaseOrderService.cancel(id);
            ra.addFlashAttribute("success", "Order cancelled");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchase-orders/" + id;
    }
}
