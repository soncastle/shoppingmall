package com.shoppingmall.order.repository;


import com.shoppingmall.order.domain.PurchaseProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseProductRepository extends JpaRepository<PurchaseProduct, Long> {
  @Query("SELECT i FROM PurchaseProduct i WHERE i.purchase.purchaseId = :purchaseId")
  List<PurchaseProduct> findByPurchaseId(@Param("purchaseId") Long purchaseId);

  @Query("SELECT d FROM PurchaseProduct d ORDER BY d.purchase.purchaseId DESC")
  Page<PurchaseProduct> findAllOrderByPurchaseIdDesc(Pageable pageable);

  @Query("SELECT d FROM PurchaseProduct d WHERE d.purchase.userId = :userId ORDER BY d.purchase.purchaseId DESC")
  Page<PurchaseProduct> findByUserIdOrderByPurchaseIdDesc(@Param("userId") String userId, Pageable pageable);

  @Query("SELECT d FROM PurchaseProduct d WHERE d.purchase.cancelAt IS NOT NULL AND d.userId = :userId ORDER BY d.purchase.purchaseId DESC")
  Page<PurchaseProduct> findByCancelAtIsNotNullAndUserIdOrderByPurchaseIdDesc(String userId, Pageable pageable);

  @Query("SELECT d FROM PurchaseProduct d WHERE d.purchase.cancelAt IS NULL AND d.userId = :userId ORDER BY d.purchase.purchaseId DESC")
  Page<PurchaseProduct> findByCancelAtIsNullAndUserIdOrderByPurchaseIdDesc(String userId, Pageable pageable);

  @Query("SELECT d FROM PurchaseProduct d ORDER BY d.purchase.purchaseId DESC")
  Page<PurchaseProduct> findByCancelAtIsNotNullOrderByPurchaseIdDesc(Pageable pageable);

  @Query("SELECT d FROM PurchaseProduct d ORDER BY d.purchase.purchaseId DESC")
  Page<PurchaseProduct> findByCancelAtIsNullOrderByPurchaseIdDesc(Pageable pageable);
}

