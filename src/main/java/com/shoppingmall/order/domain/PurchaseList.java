package com.shoppingmall.order.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)  // Auditing을 위한 리스너 추가
public class PurchaseList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseId; // 주문 ID (PK)

    private Long cartId; // 장바구니 ID (FK), 단순한 Long 타입 필드

    private String userId; // 사용자 ID (단순한 String)

    private String totalPrice; // 총 가격

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt; // 주문 생성 시간

    private LocalDateTime cancelAt; // 주문 취소 시간

    private String deliveryStatus; // 배송 상태

    private String discount; // 할인액
}