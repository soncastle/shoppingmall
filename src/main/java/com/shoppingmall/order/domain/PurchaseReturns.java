package com.shoppingmall.order.domain;


import com.shoppingmall.order.dto.CartToPuchaseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturns {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseReturnsId; // 주문 상품 ID (PK)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="purchaseProduct_Id")
    private PurchaseProduct purchaseProduct;

    private String cancelReason;

    private String userId;

    @Column(length = 1000)
    private String returnsContent;

    // 저장된 이미지 경로들을 콤마(,)로 구분하여 저장 (간단한 예시)
    private String returnsImagePaths;

    private LocalDateTime createAt;
}
