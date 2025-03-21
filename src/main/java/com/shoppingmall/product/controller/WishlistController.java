package com.shoppingmall.product.controller;

import com.shoppingmall.product.dto.ProductResponseDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shoppingmall.user.dto.ApiResponse;
import com.shoppingmall.product.dto.WishlistDTO;
import com.shoppingmall.product.model.Wishlist;
import com.shoppingmall.product.service.WishlistService;



@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

 // 찜한 상품 저장
    @PostMapping("/add")
    public ResponseEntity<?> addProductToWishlist(@RequestParam Long userId, @RequestParam Long productId) {
        Map<String , Object> data = new HashMap<>();
        try {
            System.out.println("🛒 찜 추가 요청 - userId: " + userId + ", productId: " + productId);

            Wishlist added = wishlistService.addProductToWishlist(userId, productId);
            WishlistDTO wishlistDTO = new WishlistDTO();
            wishlistDTO.setId(added.getId());
            wishlistDTO.setUserId(added.getUser().getId());
            wishlistDTO.setProductId(added.getProduct().getProductId());
            wishlistDTO.setAddedOn(added.getAddedOn());
            data.put("wishlist", wishlistDTO);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            System.out.println("❌ 오류 발생: " + e.getMessage());
            data.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(data));
        }
    }
    



    @DeleteMapping("/remove")
    public ResponseEntity<?> removeProductFromWishlist(
        @RequestParam Long userId,
        @RequestParam Long productId) {

        if (userId == null || productId == null) {
            return ResponseEntity.badRequest().body("유효하지 않은 요청입니다.");
        }

        wishlistService.removeProductFromWishlist(userId, productId);
        return ResponseEntity.ok("찜 목록에서 상품이 삭제되었습니다.");
    }

    // ✅ 로그인 없이 모든 찜 목록 조회 가능하도록 변경
    @GetMapping

    public ResponseEntity<ApiResponse<?>> showWishlist(Model model , Authentication authentication) {
      String userId = authentication.getName();
      List<ProductResponseDTO> likeProducts = wishlistService.getUserWishlists(userId);

      return ResponseEntity.ok(com.shoppingmall.user.dto.ApiResponse.success(likeProducts));
    }



}