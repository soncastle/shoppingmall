package com.shoppingmall.cart.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



import org.springframework.stereotype.Service;

import com.shoppingmall.cart.model.Cart;
import com.shoppingmall.cart.model.CartDTO;
import com.shoppingmall.cart.model.CartItem;
import com.shoppingmall.cart.model.CartItemDTO;
import com.shoppingmall.cart.repository.CartItemRepository;
import com.shoppingmall.product.model.Product;
import com.shoppingmall.product.service.ProductService;
import com.shoppingmall.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;

    // 사용자의 장바구니 가져오기
    public CartDTO getCartByUser(User user) {
        Cart cart = user.getCart();
        List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
        		.map(CartItemDTO::new)
        		.collect(Collectors.toList());
        
        
        return new CartDTO(cart.getId(), cartItemDTOs); // 최신 장바구니 반환
    } 
    
    // 장바구니에 상품 추가
    public CartDTO addProductToCart(User user, Long productId, int quantity) {
        Product product = productService.getProductById(productId);
        Cart cart = user.getCart();
        
        // 장바구니의 상품을 찾기
        Optional<CartItem> existingItem = cart.getCartItems().stream()
            .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
            .findFirst();
        
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.updateTotalPrice(); // 가격 계산 갱신
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem(cart, product, quantity, BigDecimal.valueOf(product.getPrice()), BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity)));
            cart.addCartItem(cartItem);
            cartItemRepository.save(cartItem);
        }
        return getCartByUser(user);
    }

    // 장바구니에서 상품 삭제
    public CartDTO removeProductFromCart(User user, Long cartItemId) {
        Cart cart = user.getCart();
        
        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
        cartItemRepository.deleteById(cartItemId);  // CartItem 삭제
        
        return getCartByUser(user);  // 변경된 장바구니 데이터 리턴
    }
    
    // 다중 상품 삭제
    public CartDTO removeProductsFromCart(User user, List<Long> cartItemIds) {
        Cart cart = user.getCart();

        // 각 cartItemId에 대해 삭제 처리
        for (Long cartItemId : cartItemIds) {
            cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId)); // 장바구니에서 아이템 제거
            cartItemRepository.deleteById(cartItemId);  // DB에서 삭제
        }

        return getCartByUser(user);  // 변경된 장바구니 데이터 리턴
    }

    // 장바구니 상품 수량 업데이트
    public CartDTO updateProductQuantity(User user, Long cartItemId, int quantity) {
        Cart cart = user.getCart();
        
        CartItem cartItem = cart.getCartItems().stream()
            .filter(item -> item.getId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Item not found"));

        cartItem.setQuantity(quantity);
        cartItem.updateTotalPrice();  // 가격 업데이트
        cartItemRepository.save(cartItem);  // 데이터베이스에 저장
        
        return getCartByUser(user);  // 변경된 장바구니 데이터 리턴
    }
}