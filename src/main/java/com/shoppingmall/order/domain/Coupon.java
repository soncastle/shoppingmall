package com.shoppingmall.order.domain;

import com.shoppingmall.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id; // 주문 ID (PK)

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // cascade 꼭 있어야 함! 자식 엔티티 자동 저장
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CouponList> couponList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.couponList == null || this.couponList.isEmpty()) {
            List<CouponList> couponLists = new ArrayList<>();
            CouponList couponList = new CouponList();
            couponList.setCouponName("가입 환영 쿠폰");
            couponList.setCouponComment("가입을 환영합니다!");
            couponList.setDiscount(5000);
            couponList.setCreateAt(LocalDateTime.now());

            couponList.setCoupon(this); // ★ 주인 쪽 연관관계 설정 필수! (안 하면 insert 안 됨)
            couponLists.add(couponList);
            this.couponList = couponLists;
        }
    }

}
