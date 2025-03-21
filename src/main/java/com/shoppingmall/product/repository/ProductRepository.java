package com.shoppingmall.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingmall.product.model.Category;
import com.shoppingmall.product.model.PetType;
import com.shoppingmall.product.model.Product;
import com.shoppingmall.product.model.Subcategory;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category); // 기본 카테고리 검색
    List<Product> findBySubcategory(Subcategory subcategory);
    List<Product> findByProductNameContaining(String productName);

    List<Product> findByPetType(PetType petType, Sort sort);
    List<Product> findByCategoryAndPetType(Category category, PetType petType, Sort sort);
    List<Product> findBySubcategoryAndPetType(Subcategory subcategory, PetType petType, Sort sort);
    
    // 카테고리 필터 정렬별 메서드 추가
    List<Product> findByCategoryOrderByCreatedAtDesc(Category category);	// 카테고리 최신순 (신제품순)
    List<Product> findByCategoryOrderByPriceAsc(Category category);			// 카테고리 낮은 가격순
    List<Product> findByCategoryOrderByPriceDesc(Category category);      	// 카테고리 높은 가격순
    List<Product> findByCategoryOrderByAverageRatingDesc(Category category); // 카테고리 평점순
    List<Product> findBySubcategoryOrderByPriceAsc(Subcategory subcategory); // 서브카테고리
    List<Product> findBySubcategoryOrderByPriceDesc(Subcategory subcategory);// 서브카테고리
    List<Product> findBySubcategoryOrderByAverageRatingDesc(Subcategory subcategory); // 서브카테고리
    List<Product> findBySubcategoryOrderByCreatedAtDesc(Subcategory subcategory); // 서브카테고리
    
    Page<Product> findAll(Pageable pageable);
}


