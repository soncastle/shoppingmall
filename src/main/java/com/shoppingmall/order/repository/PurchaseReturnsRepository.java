package com.shoppingmall.order.repository;

import com.shoppingmall.order.domain.PurchaseReturns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseReturnsRepository extends JpaRepository<PurchaseReturns, Long> {
   PurchaseReturns findByUserId(String userId);
}
