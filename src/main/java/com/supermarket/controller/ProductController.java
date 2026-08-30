package com.supermarket.controller;

import com.supermarket.entity.Category;
import com.supermarket.entity.Product;
import com.supermarket.service.CategoryService;
import com.supermarket.service.ProductService;
import com.supermarket.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             SupplierService supplierService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("products", productService.search(q));
        model.addAttribute("q", q);
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("mode", "create");
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Product product, BindingResult result,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long supplierId,
                         Model model, RedirectAttributes ra) {
        if (categoryId != null) {
            product.setCategory(categoryService.findById(categoryId));
        }
        if (supplierId != null) {
            product.setSupplier(supplierService.findById(supplierId));
        }
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAll());
            model.addAttribute("mode", "create");
            return "products/form";
        }
        productService.save(product);
        ra.addFlashAttribute("success", "Product created");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAll());
        model.addAttribute("mode", "edit");
        return "products/form";
    }

    @GetMapping("/{id}/qr")
    public String qr(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "products/qr";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Product product, BindingResult result,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long supplierId,
                         Model model, RedirectAttributes ra) {
        product.setId(id);
        if (categoryId != null) {
            product.setCategory(categoryService.findById(categoryId));
        }
        if (supplierId != null) {
            product.setSupplier(supplierService.findById(supplierId));
        }
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAll());
            model.addAttribute("mode", "edit");
            return "products/form";
        }
        productService.save(product);
        ra.addFlashAttribute("success", "Product updated");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Product deleted");
        return "redirect:/products";
    }

    @PostMapping("/{id}/stock")
    public String adjustStock(@PathVariable Long id, @RequestParam int delta, RedirectAttributes ra) {
        try {
            productService.adjustStock(id, delta);
            ra.addFlashAttribute("success", "Stock adjusted");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }
}
