package com.shoppingmall.cart.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.shoppingmall.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId; // PK
    
//    @OneToOne(fetch = FetchType.LAZY)  
//    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user; // 회원
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> cartItems = new ArrayList<>();
   
    public void addCartItem(CartItem cartItem) {
    	this.cartItems.add(cartItem);
    	cartItem.setCart(this);
    }
    
    public void removeCartItem(CartItem cartItem) {
    	this.cartItems.remove(cartItem);
    	cartItem.setCart(null);
    }
    
    public int getTotalQuantity() {
    	return cartItems.stream()
    					.mapToInt(CartItem::getQuantity)
    					.sum();
    }
    
    public BigDecimal getTotalPrice() {
        return cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
