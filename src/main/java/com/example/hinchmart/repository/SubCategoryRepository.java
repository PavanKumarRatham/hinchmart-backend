package com.example.hinchmart.repository;

import com.example.hinchmart.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    List<SubCategory> findByCategoryId(Long categoryId);
    Optional<SubCategory> findByName(String name);
}