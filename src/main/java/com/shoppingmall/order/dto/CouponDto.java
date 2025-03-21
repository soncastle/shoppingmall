package com.shoppingmall.order.dto;

import com.shoppingmall.order.domain.CouponList;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDto {
    private Long couponId;
    private String userId;
    private List<CouponList> couponList;

}
