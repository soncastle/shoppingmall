package com.shoppingmall.cart.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;   
    private String imageUrl;      
    private BigDecimal price;     
    private int quantity;
    @Getter
    private BigDecimal totalPrice;


    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.productId = cartItem.getProduct().getProductId();
        this.productName = cartItem.getProduct().getProductName();
        this.imageUrl = cartItem.getProduct().getImageUrls().toString();
        this.price = cartItem.getPrice();
        this.quantity = cartItem.getQuantity();
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));       
    }

}
