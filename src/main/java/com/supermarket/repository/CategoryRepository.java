package com.supermarket.repository;

import com.supermarket.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    java.util.Optional<Category> findByName(String name);
}
