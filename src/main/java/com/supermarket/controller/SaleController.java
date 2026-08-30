package com.supermarket.controller;

import com.supermarket.entity.Product;
import com.supermarket.entity.Sale;
import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import com.supermarket.service.ProductService;
import com.supermarket.service.SaleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public SaleController(SaleService saleService,
                          ProductService productService,
                          UserRepository userRepository) {
        this.saleService = saleService;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String history(Model model) {
        model.addAttribute("sales", saleService.findAll());
        return "sales/history";
    }

    @GetMapping("/pos")
    public String pos(@RequestParam(required = false) String q, Model model, HttpSession session) {
        model.addAttribute("products", productService.search(q));
        model.addAttribute("q", q);
        addCartToModel(model, session);
        return "sales/pos";
    }

    @PostMapping("/pos/add")
    public String addToCart(@RequestParam Long productId, @RequestParam int qty, HttpSession session,
                            @RequestParam(required = false) String customerName,
                            @RequestParam(required = false) BigDecimal discount,
                            @RequestParam(required = false) BigDecimal taxRate) {
        saveCartDetails(session, customerName, discount, taxRate);
        Map<Long, Integer> cart = getCart(session);
        cart.merge(productId, qty, Integer::sum);
        session.setAttribute("cart", cart);
        return "redirect:/sales/pos";
    }

    @PostMapping("/pos/remove")
    public String removeFromCart(@RequestParam Long productId, HttpSession session,
                                 @RequestParam(required = false) String customerName,
                                 @RequestParam(required = false) BigDecimal discount,
                                 @RequestParam(required = false) BigDecimal taxRate) {
        saveCartDetails(session, customerName, discount, taxRate);
        Map<Long, Integer> cart = getCart(session);
        cart.remove(productId);
        session.setAttribute("cart", cart);
        return "redirect:/sales/pos";
    }

    @PostMapping("/pos/scan")
    public String scan(@RequestParam String code, @RequestParam(required = false, defaultValue = "1") int qty,
                       HttpSession session, RedirectAttributes ra,
                       @RequestParam(required = false) String customerName,
                       @RequestParam(required = false) BigDecimal discount,
                       @RequestParam(required = false) BigDecimal taxRate) {
        saveCartDetails(session, customerName, discount, taxRate);
        Product product = productService.findByScanCode(code);
        if (product == null) {
            ra.addFlashAttribute("error", "No product found for code '" + code + "'");
            return "redirect:/sales/pos";
        }
        Map<Long, Integer> cart = getCart(session);
        cart.merge(product.getId(), qty, Integer::sum);
        session.setAttribute("cart", cart);
        return "redirect:/sales/pos";
    }

    @PostMapping("/pos/checkout")
    public String checkout(Authentication auth, HttpSession session, RedirectAttributes ra,
                           @RequestParam(required = false) String customerName,
                           @RequestParam(required = false) BigDecimal discount,
                           @RequestParam(required = false) BigDecimal taxRate) {
        Map<Long, Integer> cart = getCart(session);
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Cart is empty");
            return "redirect:/sales/pos";
        }
        try {
            String username = auth.getName();
            User cashier = userRepository.findByUsername(username).orElseThrow();
            List<Long> ids = new ArrayList<>(cart.keySet());
            List<Integer> qtys = new ArrayList<>(cart.values());
            String cust = customerName != null ? customerName : (String) session.getAttribute("cartCustomerName");
            BigDecimal disc = discount != null ? discount : (BigDecimal) session.getAttribute("cartDiscount");
            BigDecimal tax = taxRate != null ? taxRate : (BigDecimal) session.getAttribute("cartTaxRate");
            Sale sale = saleService.checkout(ids, qtys, cashier, cust, disc, tax);
            session.removeAttribute("cart");
            session.removeAttribute("cartCustomerName");
            session.removeAttribute("cartDiscount");
            session.removeAttribute("cartTaxRate");
            ra.addFlashAttribute("success", "Sale completed - Receipt " + sale.getReceiptNumber());
            return "redirect:/sales/" + sale.getId();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sales/pos";
        }
    }

    @GetMapping("/{id}")
    public String receipt(@PathVariable Long id, Model model) {
        model.addAttribute("sale", saleService.findById(id));
        return "sales/receipt";
    }

    private void addCartToModel(Model model, HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        List<CartItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product p = productService.findById(entry.getKey());
            CartItem item = new CartItem(p, entry.getValue());
            items.add(item);
            total = total.add(item.getLineTotal());
        }
        model.addAttribute("cart", items);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartSubtotal", total);
        model.addAttribute("cartDiscount", session.getAttribute("cartDiscount"));
        model.addAttribute("cartTaxRate", session.getAttribute("cartTaxRate"));
        model.addAttribute("cartCustomerName", session.getAttribute("cartCustomerName"));
    }

    private Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
        }
        return cart;
    }

    private void saveCartDetails(HttpSession session, String customerName, BigDecimal discount, BigDecimal taxRate) {
        if (customerName != null) {
            session.setAttribute("cartCustomerName", customerName);
        }
        if (discount != null) {
            session.setAttribute("cartDiscount", discount);
        }
        if (taxRate != null) {
            session.setAttribute("cartTaxRate", taxRate);
        }
    }

    public static class CartItem {
        private final Product product;
        private final int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getLineTotal() {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
