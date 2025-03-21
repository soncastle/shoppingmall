package com.shoppingmall.order.repository;

import com.shoppingmall.order.domain.CouponList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponListRepository extends JpaRepository<CouponList, Long> {
}
