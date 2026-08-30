package com.supermarket.controller;

import com.supermarket.entity.Supplier;
import com.supermarket.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("suppliers", supplierService.findAll());
        return "suppliers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Supplier supplier, BindingResult result,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        supplierService.save(supplier);
        ra.addFlashAttribute("success", "Supplier created");
        return "redirect:/suppliers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("supplier", supplierService.findById(id));
        return "suppliers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Supplier supplier,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        supplier.setId(id);
        supplierService.save(supplier);
        ra.addFlashAttribute("success", "Supplier updated");
        return "redirect:/suppliers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        supplierService.delete(id);
        ra.addFlashAttribute("success", "Supplier deleted");
        return "redirect:/suppliers";
    }
}
