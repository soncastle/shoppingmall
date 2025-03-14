package com.shoppingmall.order.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class PurchaseDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId; // 배송 ID (PK)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id") // 주문 ID (FK)
    private Purchase purchase;

    private String userId;

    private String receiverName; // 수령인 이름

    private String receiverPhone; // 수령인 전화번호

    private String receiverAddr; // 수령인 주소

    private String deliveryMessage; // 배송 메시지

    private String deliveryStatus; // 배송 상태

    private LocalDateTime cancelAt; // 주문 취소 시간

    private LocalDateTime deliveredAt; // 배송 완료 시간

}
