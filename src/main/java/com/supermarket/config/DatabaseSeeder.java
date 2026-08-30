package com.supermarket.config;

import com.supermarket.entity.*;
import com.supermarket.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               SupplierRepository supplierRepository,
                               ProductRepository productRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }

            if (supplierRepository.count() == 0) {
                Supplier s1 = new Supplier();
                s1.setCompanyName("Fresh Farms Produce");
                s1.setContactName("Maria Garcia");
                s1.setEmail("sales@freshfarms.com");
                s1.setPhone("555-0101");
                s1.setTaxId("TIN-100-001");
                s1.setPaymentTerms("Net 30");
                supplierRepository.save(s1);

                Supplier s2 = new Supplier();
                s2.setCompanyName("Global Beverages Co.");
                s2.setContactName("John Smith");
                s2.setEmail("orders@globalbev.com");
                s2.setPhone("555-0102");
                s2.setTaxId("TIN-100-002");
                s2.setPaymentTerms("Net 15");
                supplierRepository.save(s2);

                Supplier s3 = new Supplier();
                s3.setCompanyName("Home Essentials Ltd.");
                s3.setContactName("Alice Johnson");
                s3.setEmail("contact@homeessentials.com");
                s3.setPhone("555-0103");
                s3.setTaxId("TIN-100-003");
                s3.setPaymentTerms("Net 45");
                supplierRepository.save(s3);
            }

            if (categoryRepository.count() == 0) {
                categoryRepository.save(new Category("Produce", "Fresh fruits and vegetables"));
                categoryRepository.save(new Category("Beverages", "Drinks, juices and bottled water"));
                categoryRepository.save(new Category("Dairy", "Milk, cheese, yogurt and eggs"));
                categoryRepository.save(new Category("Household", "Cleaning and home products"));
                categoryRepository.save(new Category("Snacks", "Chips, sweets and quick bites"));
            }

            if (productRepository.count() == 0) {
                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Fresh Apple - 1kg", "FRU-001", "1234567890123",
                        "Crisp red apples", new BigDecimal("3.99"), new BigDecimal("2.10"), 120, 20,
                        "Produce", "Fresh Farms Produce", "FarmFresh", "kg", new BigDecimal("0"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Banana - 1kg", "FRU-002", "1234567890124",
                        "Ripe yellow bananas", new BigDecimal("1.49"), new BigDecimal("0.80"), 80, 15,
                        "Produce", "Fresh Farms Produce", "FarmFresh", "kg", new BigDecimal("0"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Orange Juice 1L", "BEV-001", "1234567890125",
                        "Premium squeezed orange juice", new BigDecimal("2.99"), new BigDecimal("1.70"),
                        4, 10,                         "Beverages", "Global Beverages Co.", "SunJuice", "L", new BigDecimal("18"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Sparkling Water 500ml", "BEV-002", "1234567890126",
                        "Still and sparkling mineral water", new BigDecimal("1.25"), new BigDecimal("0.55"),
                        200, 30,                         "Beverages", "Global Beverages Co.", "AquaPure", "bottle", new BigDecimal("18"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Whole Milk 1L", "DRY-001", "1234567890127",
                        "Fresh whole milk", new BigDecimal("1.89"), new BigDecimal("1.10"), 60, 15,
                        "Dairy", "Fresh Farms Produce", "DairyBest", "L", new BigDecimal("0"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Cheddar Cheese 200g", "DRY-002", "1234567890128",
                        "Aged cheddar cheese block", new BigDecimal("4.49"), new BigDecimal("2.90"), 3, 8,
                        "Dairy", "Fresh Farms Produce", "DairyBest", "pack", new BigDecimal("0"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Laundry Detergent 1L", "HOU-001", "1234567890129",
                        "Liquid laundry detergent", new BigDecimal("5.99"), new BigDecimal("3.80"), 35, 10,
                        "Household", "Home Essentials Ltd.", "CleanHome", "L", new BigDecimal("18"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Paper Towels (3 pack)", "HOU-002", "1234567890130",
                        "Strong absorbent paper towels", new BigDecimal("4.79"), new BigDecimal("2.95"), 50, 12,
                        "Household", "Home Essentials Ltd.", "CleanHome", "pack", new BigDecimal("18"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Potato Chips 150g", "SNK-001", "1234567890131",
                        "Classic salted potato chips", new BigDecimal("2.49"), new BigDecimal("1.30"), 100, 25,
                        "Snacks", "Global Beverages Co.", "CrispyCo", "bag", new BigDecimal("18"));

                seedProduct(productRepository, categoryRepository, supplierRepository,
                        "Chocolate Bar 100g", "SNK-002", "1234567890132",
                        "Dark chocolate bar", new BigDecimal("2.19"), new BigDecimal("1.15"), 2, 10,
                        "Snacks", "Global Beverages Co.", "SweetCo", "bar", new BigDecimal("18"));
            }
        };
    }

    private void seedProduct(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             SupplierRepository supplierRepository,
                             String name, String sku, String barcode, String description,
                             BigDecimal price, BigDecimal cost, int stock, int reorderLevel,
                             String categoryName, String supplierName,
                             String brand, String unit, BigDecimal taxRate) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setBarcode(barcode);
        p.setDescription(description);
        p.setPrice(price);
        p.setCost(cost);
        p.setStock(stock);
        p.setReorderLevel(reorderLevel);
        p.setBrand(brand);
        p.setUnitOfMeasure(unit);
        p.setTaxRate(taxRate);
        p.setCategory(categoryRepository.findByName(categoryName).orElse(null));
        p.setSupplier(supplierRepository.findByCompanyName(supplierName).orElse(null));
        productRepository.save(p);
    }
}
