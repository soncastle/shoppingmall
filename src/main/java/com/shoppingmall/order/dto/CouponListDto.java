package com.shoppingmall.order.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponListDto {
    private Long couponId;
    private int discount;
    private String couponComment;
    private String couponName;
    private LocalDateTime createAt;
    private LocalDateTime usedAt;
}
