package com.shoppingmall.cart.repository;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shoppingmall.cart.model.Cart;
import com.shoppingmall.cart.model.CartItem;
import com.shoppingmall.product.model.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
	
	//ProductIdIn: Product 엔티티의 productId 컬럼을 기준으로 여러 개의 productIds를 받아서 해당하는 CartItem들을 반환하는 역할
	List<CartItem> findByCartAndProduct_ProductIdIn(Cart cart, List<Long> productIds);

	@Modifying
	@Transactional
	@Query("DELETE FROM CartItem ci WHERE ci.product.productId IN :productId")
	void deleteByProductIds(@Param("productId") Long productId);
}