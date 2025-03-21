package com.shoppingmall.order.domain;


import com.shoppingmall.product.model.Product;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class PurchaseProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseProductId; // 주문 상품 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id") // 주문 ID (FK)
    private Purchase purchase;

    private String imageUrl;

    private Long productId;

    private String userId;

    private String productName;

    private String option; // 상품 옵션

    private int quantity; // 수량
    
    private int price; // 가격

//    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt; // 주문 생성 시간

    private String cancelReason;

    private LocalDateTime cancelAt; // 주문 취소 시간

    private int totalPrice; // 해당 상품 총 가격

    private String deliveryStatus;

    private String purchaseConform;

//    private LocalDateTime review;
}
