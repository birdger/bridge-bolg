package com.example.blog.repository;

import com.example.blog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsBySlug(String slug);
    List<Category> findByEnabledTrueOrderByNameAsc();
}
