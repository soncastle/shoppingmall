package com.shoppingmall.product.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.product.model.Category;
import com.shoppingmall.product.model.PetType;
import com.shoppingmall.product.model.Product;
import com.shoppingmall.product.model.Review;
import com.shoppingmall.product.model.Subcategory;
import com.shoppingmall.product.repository.ReviewRepository;
import com.shoppingmall.product.service.CategoryService;
import com.shoppingmall.product.service.ProductService;
import com.shoppingmall.product.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    public ProductController(ProductService productService, CategoryService categoryService, ReviewService reviewService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/products")
    @Operation(summary = "상품 목록 조회", description = "모든 상품을 조회하거나 petType으로 필터링합니다.")
    public String listProducts(@RequestParam(defaultValue = "newest") String sort,
                               @RequestParam(required = false) PetType petType, Model model) {
        List<Product> products;
        if (petType != null) {
            products = productService.getProductsByPetTypeSorted(petType, sort);
        } else {
            products = productService.listAllProductsSorted(sort);
        }
        for (Product product : products) {
            int reviewCount = reviewService.getReviewsByProductId(product.getProductId()).size();
            product.setReviewCount(reviewCount);
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("selectedPetType", petType);
        model.addAttribute("sort", sort);
        return "/product/index2";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("subcategories", categoryService.findAllSubcategories());
        model.addAttribute("product", new Product());
        return "/product/addProduct";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute("product") Product product,
                             @RequestParam("categoryId") Long categoryId,
                             @RequestParam("subcategoryId") Long subcategoryId,
                             @RequestParam("petType") PetType petType,
                             @RequestParam("mainImageFiles") List<MultipartFile> mainImageFiles,
                             @RequestParam("detailImageFiles") List<MultipartFile> detailImageFiles) {
        List<String> mainImageUrls = uploadFiles(mainImageFiles);
        List<String> detailImageUrls = uploadFiles(detailImageFiles);
        product.setPetType(petType);
        product.setImageUrls(mainImageUrls);
        product.setDetailImageUrls(detailImageUrls);
        productService.saveProduct(product, categoryId, subcategoryId, mainImageUrls, detailImageUrls);
        return "redirect:/products";
    }

    private String uploadFile(MultipartFile file) {
        if (!file.isEmpty()) {
            String basePath = new File("src/main/resources/static/images").getAbsolutePath();
            String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
            String filePath = basePath + File.separator + fileName;
            File destinationFile = new File(filePath);
            try {
                file.transferTo(destinationFile);
                return "/images/" + fileName;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    
    private List<String> uploadFiles(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = uploadFile(file);
            if (imageUrl != null) {
                imageUrls.add(imageUrl);
            }
        }
        return imageUrls;
    }
    
    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable("id") Long productId,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Review> reviewsPage = reviewService.getPagedReviewsByProductId(productId, pageRequest);
        Product product = productService.getProductById(productId);
        List<Review> allReviews = reviewService.getReviewsByProductId(productId);
        int totalReviews = allReviews.size();
        int count5 = (int) allReviews.stream().filter(r -> r.getRating() == 5).count();
        int count4 = (int) allReviews.stream().filter(r -> r.getRating() == 4).count();
        int count3 = (int) allReviews.stream().filter(r -> r.getRating() == 3).count();
        int count2 = (int) allReviews.stream().filter(r -> r.getRating() == 2).count();
        int count1 = (int) allReviews.stream().filter(r -> r.getRating() == 1).count();
        BigDecimal averageRating = BigDecimal.ZERO;
        if (totalReviews > 0) {
            averageRating = allReviews.stream()
                .map(r -> BigDecimal.valueOf(r.getRating()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalReviews), 2, RoundingMode.HALF_UP);
        }
        int rounded = averageRating.setScale(0, RoundingMode.HALF_UP).intValue();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rounded; i++) sb.append("⭐");
        for (int i = rounded; i < 5; i++) sb.append("☆");
        String averageStars = sb.toString();
        Map<Long, List<Map<String, Object>>> reviewComments = new HashMap<>();
        for (Review r : reviewsPage.getContent()) {
            try {
                reviewComments.put(r.getReviewId(), reviewService.getComments(r.getReviewId()));
            } catch (Exception e) {
                reviewComments.put(r.getReviewId(), List.of());
            }
        }
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("count5", count5);
        model.addAttribute("count4", count4);
        model.addAttribute("count3", count3);
        model.addAttribute("count2", count2);
        model.addAttribute("count1", count1);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("averageStars", averageStars);
        model.addAttribute("product", product);
        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("reviewComments", reviewComments);
        return "/product/productDetail";
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("subcategories", categoryService.findAllSubcategories());
        return "/product/editProduct";
    }
    
    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @ModelAttribute("product") Product product,
                              @RequestParam("categoryId") Long categoryId,
                              @RequestParam("subcategoryId") Long subcategoryId,
                              @RequestParam(value = "mainImageFiles", required = false) List<MultipartFile> mainImageFiles,
                              @RequestParam(value = "detailImageFiles", required = false) List<MultipartFile> detailImageFiles) {
        Product existingProduct = productService.getProductById(id);
        product.setProductId(existingProduct.getProductId());
        product.setCategory(categoryService.findCategoryById(categoryId));
        product.setSubcategory(categoryService.findSubcategoryById(subcategoryId));
        List<String> mainImageUrls;
        if (mainImageFiles != null && !mainImageFiles.isEmpty()) {
            mainImageUrls = uploadFiles(mainImageFiles);
        } else {
            mainImageUrls = existingProduct.getImageUrls();
        }
        product.setImageUrls(mainImageUrls);
        List<String> detailImageUrls = (detailImageFiles != null && !detailImageFiles.isEmpty()) ?
                uploadFiles(detailImageFiles) : existingProduct.getDetailImageUrls();
        product.setDetailImageUrls(detailImageUrls);
        productService.updateProduct(id, product, categoryId, subcategoryId, mainImageUrls, detailImageUrls);
        return "redirect:/products/" + id;
    }
    
    @DeleteMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
    
    @GetMapping("/products/category/{categoryId}")
    public String getProductsByCategory(@PathVariable("categoryId") Long categoryId,
                                        @RequestParam(required = false) PetType petType,
                                        @RequestParam(required = false, defaultValue = "newest") String sort,
                                        Model model) {
        Category category = categoryService.findCategoryById(categoryId);
        List<Product> products;
        if (petType != null) {
            products = productService.getProductsByCategoryAndPetTypeSorted(categoryId, petType, sort);
        } else {
            products = productService.findProductsByCategory(category, sort);
        }
        List<Subcategory> subcategories = categoryService.findSubcategoriesByCategory(category);
        model.addAttribute("currentCategory", category);
        model.addAttribute("products", products);
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("selectedPetType", petType);
        model.addAttribute("sort", sort);
        return "product/index2";
    }
    
    @GetMapping("/products/category/{categoryId}/json")
    public ResponseEntity<List<Product>> getProductsByCategoryJson(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(defaultValue = "newest") String sort) {
        Category category = categoryService.findCategoryById(categoryId);
        List<Product> products = productService.findProductsByCategory(category, sort);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/subcategory/{subId}")
    public String getProductsBySubcategory(@PathVariable("subId") Long subId,
                                           @RequestParam(required = false) PetType petType,
                                           @RequestParam(required = false, defaultValue = "newest") String sort,
                                           Model model) {
        Subcategory subcategory = categoryService.findSubcategoryById(subId);
        List<Product> products;
        if (petType != null) {
            products = productService.getProductsBySubcategoryAndPetTypeSorted(subId, petType, sort);
        } else {
            products = productService.findProductsBySubcategory(subcategory, sort);
        }
        Category parentCategory = subcategory.getCategory();
        List<Subcategory> subcategories = categoryService.findSubcategoriesByCategory(parentCategory);
        model.addAttribute("currentCategory", parentCategory);
        model.addAttribute("products", products);
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("selectedPetType", petType);
        model.addAttribute("sort", sort);
        return "product/index2";
    }
    
    @GetMapping("/products/search")
    public String searchProducts(@RequestParam("search") String search, Model model) {
        List<Product> products = productService.searchProducts(search);
        model.addAttribute("products", products);
        return "/product/index2";
    }
    
    @PostMapping("/products/{productId}/reviews")
    public String addReview(@PathVariable Long productId, @ModelAttribute Review review, @RequestParam("imageFile") MultipartFile imageFile) {
        review.setProductId(productId);
        review.setUserId(1L);
        reviewService.saveReview(review, imageFile);
        return "redirect:/products/" + productId;
    }
    
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping("/products/{productId}/reviews/{reviewId}/comments")
    @ResponseBody
    public Map<String, Object> addCommentAjax(
        @PathVariable Long productId,
        @PathVariable Long reviewId,
        @RequestParam String content,
        @RequestParam Long userId
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            reviewService.addComment(reviewId, content, userId);
            result.put("userId", userId);
            result.put("content", content);
            result.put("createdAt", new Date());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result.put("error", "댓글 추가에 실패하였습니다: " + e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/{productId}/reviews/{reviewId}/comments")
    @ResponseBody
    public List<Map<String, Object>> getComments(@PathVariable Long productId,
                                                 @PathVariable Long reviewId) throws JsonProcessingException {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || review.getComments() == null || review.getComments().isEmpty()) {
            return new ArrayList<>();
        }
        return new ObjectMapper().readValue(review.getComments(), new TypeReference<List<Map<String, Object>>>() {});
    }
    
    @PostMapping("/products/{productId}/reviews/edit/{reviewId}")
    public String editReview(@PathVariable Long productId,
                             @PathVariable Long reviewId,
                             @ModelAttribute Review review,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        reviewService.updateReview(reviewId, review, imageFile);
        return "redirect:/products/" + productId;
    }
    
    @DeleteMapping("/products/{productId}/reviews/delete/{reviewId}")
    public String deleteReview(@PathVariable Long productId,
                               @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return "redirect:/products/" + productId;
    }
}
