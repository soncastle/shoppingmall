package com.shoppingmall.cart.controller;

import com.shoppingmall.cart.model.CartItem;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;
import com.shoppingmall.cart.model.CartDTO;
import com.shoppingmall.cart.service.CartService;
import com.shoppingmall.user.model.User;
import com.shoppingmall.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
	
    private final CartService cartService;
    private final UserRepository userRepository;

    // userId 추출
    private String getUserId(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping
    public String getCart(Authentication authentication, Model model , HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId);
        CartDTO cartDTO = cartService.getCartByUser(user);

        // 총 금액 계산
        BigDecimal totalPrice = cartDTO.getTotalPrice();
        updateCartSession(user , session);
        model.addAttribute("cartItems", cartDTO.getCartItems());
        model.addAttribute("totalPrice", cartDTO.getTotalPrice());

        return "cart/cart";
    }

    // 상품 장바구니에 추가
    @PostMapping("/items/{productId}/add")
    @ResponseBody
    public ResponseEntity<CartDTO> addToCart(Authentication authentication, @PathVariable Long productId, @RequestParam int quantity) {

        String userId = getUserId(authentication);
        User user = userRepository.findByUserId(userId);

        // 장바구니에 상품 추가

        CartDTO updatedCartDTO = cartService.addProductToCart(user, productId, quantity);
        
        return ResponseEntity.ok(updatedCartDTO); 

    }

    // 상품 삭제
    @DeleteMapping("/items/{cartItemId}/remove")
    public  ResponseEntity<CartDTO> removeFormCart(Authentication authentication, @PathVariable Long cartItemId , HttpSession session) {
        String userId = getUserId(authentication);
        User user = userRepository.findByUserId(userId);


        // 장바구니에서 상품 삭제
        CartDTO updatedCart = cartService.removeProductFromCart(user, cartItemId);
        updateCartSession(user , session);
        // 삭제 후 리다이렉트 
        return ResponseEntity.ok(updatedCart);
    }
    
    // 다중 삭제
    @DeleteMapping("/items/remove")
    public ResponseEntity<CartDTO> removeItemsFromCart(Authentication authentication, @RequestBody List<Long> cartItemIds , HttpSession session) {
        String userId = getUserId(authentication);
        User user = userRepository.findByUserId(userId);

        // 다중 삭제 처리
        CartDTO updatedCart = cartService.removeProductsFromCart(user, cartItemIds);
        updateCartSession(user , session);
        // 삭제 후 리다이렉트
        return ResponseEntity.ok(updatedCart);
    }
    

    // 수량 변경
    @PutMapping("/items/{cartItemId}/update")
    public ResponseEntity<CartDTO> updateQuantity(Authentication authentication, @PathVariable Long cartItemId, @RequestParam int quantity) {
        String userId = getUserId(authentication);
        User user = userRepository.findByUserId(userId);

        // 장바구니 상품 수량 업데이트
        CartDTO updatedCartDTO = cartService.updateProductQuantity(user, cartItemId, quantity); // 수정된 장바구니 데이터
        
        return ResponseEntity.ok(updatedCartDTO);
    }

    private void updateCartSession(User user, HttpSession session) {
        CartDTO cartDTO = cartService.getCartByUser(user);
        session.setAttribute("cartCount", cartDTO.getCartItems().size());
    }
}

