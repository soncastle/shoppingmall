package com.shoppingmall.order.repository;

import com.shoppingmall.order.domain.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("SELECT d FROM Coupon d ORDER BY d.user.userId DESC")
    Page<Coupon> findByUserId(String userId, Pageable pageable);
}
