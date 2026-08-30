package com.supermarket.controller;

import com.supermarket.entity.Role;
import com.supermarket.entity.User;
import com.supermarket.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final UserService userService;

    public EmployeeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", userService.findAll());
        return "employees/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "employees/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute User user, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (result.hasErrors() || (user.getRole() == null)) {
            model.addAttribute("roles", Role.values());
            return "employees/form";
        }
        try {
            userService.save(user);
            ra.addFlashAttribute("success", "Employee account created");
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("error", e.getMessage());
            return "employees/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", Role.values());
        return "employees/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute User user,
                         BindingResult result, @RequestParam(required = false) String password,
                         Model model, RedirectAttributes ra) {
        User existing = userService.findById(id);
        user.setId(id);
        user.setUsername(existing.getUsername());
        if (password == null || password.isBlank()) {
            user.setPassword(existing.getPassword());
        }
        if (result.hasErrors() || user.getRole() == null) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("error", "Please fix validation errors");
            return "employees/form";
        }
        userService.save(user);
        ra.addFlashAttribute("success", "Employee updated");
        return "redirect:/employees";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.delete(id);
            ra.addFlashAttribute("success", "Employee removed");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not remove employee");
        }
        return "redirect:/employees";
    }
}
