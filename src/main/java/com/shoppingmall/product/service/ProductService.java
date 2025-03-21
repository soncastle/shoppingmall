package com.shoppingmall.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.shoppingmall.product.model.Category;
import com.shoppingmall.product.model.PetType;
import com.shoppingmall.product.model.Product;
import com.shoppingmall.product.model.Subcategory;
import com.shoppingmall.product.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional  
public class ProductService {
	
    private final ProductRepository productRepository;  // 상품 데이터 접근을 위한 레포지토리
    private final CategoryService categoryService;  // 카테고리 관련 서비스

    // 전체 상품 목록 조회
    public List<Product> listAllProducts() {
        return productRepository.findAll();  // 모든 상품 조회
    }

    // 상품 등록 처리
    public Product saveProduct(Product product, Long categoryId, Long subcategoryId, 
                                 List<String> mainImageUrls, List<String> detailImageUrls) {
        product.setCategory(categoryService.findCategoryById(categoryId));
        product.setSubcategory(categoryService.findSubcategoryById(subcategoryId));
        product.setImageUrls(mainImageUrls);         // 대표 이미지 리스트 설정
        product.setDetailImageUrls(detailImageUrls);   // 상세 이미지 리스트 설정
        return productRepository.save(product);
    }

    
    // 상품 ID로 상품 조회
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));  // 상품이 없으면 예외 발생
    }

    // 상품 수정 처리
    public Product updateProduct(Long id, Product newProduct, Long categoryId, Long subcategoryId,
    							 List<String> mainImageUrls, List<String> detailImageUrls) {
	Product product = getProductById(id);
	Optional.ofNullable(newProduct.getProductName()).ifPresent(product::setProductName);
	Optional.ofNullable(newProduct.getPrice()).ifPresent(product::setPrice);
	Optional.ofNullable(newProduct.getOption()).ifPresent(product::setOption);
	Optional.ofNullable(newProduct.getContent()).ifPresent(product::setContent);
	Optional.ofNullable(newProduct.getDescription()).ifPresent(product::setDescription);
	// 단일 imageUrl는 사용하지 않거나 삭제하는 것을 고려
	product.setImageUrls(mainImageUrls);
	product.setDetailImageUrls(detailImageUrls);
	product.setCategory(categoryService.findCategoryById(categoryId));
	product.setSubcategory(categoryService.findSubcategoryById(subcategoryId));
	
	return productRepository.save(product);
	}

    
    // 상품 삭제 처리
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);  // 상품 삭제
    }
    
    // 카테고리 이름으로 상품 조회
    public List<Product> findProductsByCategoryName(String categoryName) {
    	Category category = categoryService.findCategoryByName(categoryName);  // 카테고리 조회
    	return productRepository.findByCategory(category);  // 해당 카테고리에 속하는 상품 조회
    }

    // 상품명으로 상품 검색
    public List<Product> searchProducts(String keyword) {
    	return productRepository.findByProductNameContaining(keyword);  // 상품명이 키워드를 포함하는 상품 검색
    }
    
    // 카테고리별로 상품 정렬 조회
    public List<Product> findProductsByCategory(Category category, String sort) {
        switch (sort) {
            case "priceLowHigh":  // 가격 오름차순
                return productRepository.findByCategoryOrderByPriceAsc(category);
            case "priceHighLow":  // 가격 내림차순
                return productRepository.findByCategoryOrderByPriceDesc(category);
            case "rating":  // 평점 내림차순
                return productRepository.findByCategoryOrderByAverageRatingDesc(category);
            default:  // 기본 정렬: 신제품순
                return productRepository.findByCategoryOrderByCreatedAtDesc(category);
        }
    }
 
    // 전체 상품을 정렬 기준에 맞춰 조회
    public List<Product> listAllProductsSorted(String sort) {
        switch (sort) {
            case "priceLowHigh":  // 가격 오름차순
                return productRepository.findAll(Sort.by(Sort.Direction.ASC, "price"));
            case "priceHighLow":  // 가격 내림차순
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "price"));
            case "rating":  // 평점 내림차순
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "averageRating"));
            default:  // 기본 정렬: 신제품순
                return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    // 서브카테고리별로 상품 정렬 조회
    public List<Product> findProductsBySubcategory(Subcategory subcategory, String sort) {
        switch (sort) {
            case "priceLowHigh":  // 가격 오름차순
                return productRepository.findBySubcategoryOrderByPriceAsc(subcategory);
            case "priceHighLow":  // 가격 내림차순
                return productRepository.findBySubcategoryOrderByPriceDesc(subcategory);
            case "rating":  // 평점 내림차순
                return productRepository.findBySubcategoryOrderByAverageRatingDesc(subcategory);
            default:  // 기본 정렬: 신제품순
                return productRepository.findBySubcategoryOrderByCreatedAtDesc(subcategory);
        }
    }
    


    
 // 카테고리 + petType + 정렬
    public List<Product> getProductsByCategoryAndPetTypeSorted(Long categoryId, PetType petType, String sort) {
        Category category = categoryService.findCategoryById(categoryId);
        switch (sort) {
            case "priceLowHigh":
                return productRepository.findByCategoryAndPetType(category, petType, Sort.by(Sort.Direction.ASC, "price"));
            case "priceHighLow":
                return productRepository.findByCategoryAndPetType(category, petType, Sort.by(Sort.Direction.DESC, "price"));
            case "rating":
                return productRepository.findByCategoryAndPetType(category, petType, Sort.by(Sort.Direction.DESC, "averageRating"));
            default: // newest
                return productRepository.findByCategoryAndPetType(category, petType, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    // 서브카테고리 + petType + 정렬
    public List<Product> getProductsBySubcategoryAndPetTypeSorted(Long subcategoryId, PetType petType, String sort) {
        Subcategory subcategory = categoryService.findSubcategoryById(subcategoryId);
        switch (sort) {
            case "priceLowHigh":
                return productRepository.findBySubcategoryAndPetType(subcategory, petType, Sort.by(Sort.Direction.ASC, "price"));
            case "priceHighLow":
                return productRepository.findBySubcategoryAndPetType(subcategory, petType, Sort.by(Sort.Direction.DESC, "price"));
            case "rating":
                return productRepository.findBySubcategoryAndPetType(subcategory, petType, Sort.by(Sort.Direction.DESC, "averageRating"));
            default: // newest
                return productRepository.findBySubcategoryAndPetType(subcategory, petType, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    
    public List<Product> getProductsByPetTypeSorted(PetType petType, String sort) {
        switch (sort) {
            case "priceLowHigh":
                return productRepository.findByPetType(petType, Sort.by(Sort.Direction.ASC, "price"));
            case "priceHighLow":
                return productRepository.findByPetType(petType, Sort.by(Sort.Direction.DESC, "price"));
            case "rating":
                return productRepository.findByPetType(petType, Sort.by(Sort.Direction.DESC, "averageRating"));
            default: // 기본 정렬: 신제품순
                return productRepository.findByPetType(petType, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

}
