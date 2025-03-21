package com.shoppingmall.product.service;

import com.shoppingmall.product.dto.ProductResponseDTO;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.shoppingmall.product.model.Product;
import com.shoppingmall.product.model.Wishlist;
import com.shoppingmall.product.repository.ProductRepository;
import com.shoppingmall.product.repository.WishlistRepository;
import com.shoppingmall.user.model.User;
import com.shoppingmall.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Wishlist addProductToWishlist(Long userId, Long productId) throws Exception {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception("User not found"));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new Exception("Product not found"));


        // ✅ 같은 유저가 같은 상품을 중복 추가하지 않도록 체크
        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserIdAndProductProductId(userId, productId);
        if (existingWishlist.isPresent()) {
            throw new Exception("이미 찜한 상품입니다.");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        return wishlistRepository.save(wishlist);
    }


    @Transactional  // ✅ 트랜잭션 적용
    public void removeProductFromWishlist(Long userId, Long productId) {
        Optional<Wishlist> wishlist = wishlistRepository.findByUserIdAndProductProductId(userId, productId);

        if (wishlist.isPresent()) {
            wishlistRepository.deleteByUserIdAndProductProductId(userId, productId);
        } else {
            throw new RuntimeException("찜한 상품을 찾을 수 없습니다.");
        }
    }

    // 로그인 없이도 찜 목록 전체 조회 가능하도록 수정
    public List<ProductResponseDTO> getUserWishlists(String userId ) {
        User user = userRepository.findByUserId(userId);
        List<Wishlist> wishes = wishlistRepository.findTop5ByUserId(user.getId() , PageRequest.of(0,5));
        return wishes.stream()
            .map(Wishlist::getProduct)
            .map(ProductResponseDTO :: toDTO).toList();

    }




}