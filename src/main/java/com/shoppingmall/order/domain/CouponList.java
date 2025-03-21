package com.shoppingmall.order.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couponList_id")
    private Long id;

    private String couponName;
    private int discount;
    private String couponComment;
    private LocalDateTime createAt;
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id") // coupon_id FK
    private Coupon coupon; // 양방향 관계의 주인
}
