package com.supermarket.controller;

import com.supermarket.entity.Category;
import com.supermarket.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Category category, BindingResult result,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        categoryService.save(category);
        ra.addFlashAttribute("success", "Category created");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Category category,
                         BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        category.setId(id);
        categoryService.save(category);
        ra.addFlashAttribute("success", "Category updated");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("success", "Category deleted");
        return "redirect:/categories";
    }
}
