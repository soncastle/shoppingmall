package com.shoppingmall.order.repository;


import com.shoppingmall.order.domain.PurchaseProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

  @Modifying
  @Query("UPDATE PurchaseProduct d SET d.deliveryStatus = :deliveryStatus WHERE d.purchaseProductId= :purchaseProductId")
  void updateDeliveryStatus(@Param("purchaseProductId") Long purchaseProductId, @Param("deliveryStatus") String deliverStatus);

    List<PurchaseProduct> findByPurchaseProductId(Long purchaseProductId);

    Page<PurchaseProduct> findByDeliveryStatusAndUserIdOrderByPurchaseProductIdDesc(String deliveryStatus, String userId, Pageable pageable);

  Page<PurchaseProduct> findByCancelAtIsNotNullAndUserIdOrderByPurchaseProductIdDesc(String userId, Pageable pageable);

  @Query("SELECT COUNT(d) FROM PurchaseProduct d WHERE d.userId = :userId AND d.deliveryStatus = '배송중'")
  int countByUserIdAndDeliveryStatus(@Param("userId")String userId);

  @Query("SELECT p FROM PurchaseProduct p WHERE p.deliveryStatus = '배송중' AND p.userId = :userId ORDER BY p.purchase.purchaseId DESC")
  Page<PurchaseProduct> findByUserIdAndDeliveryStatusOrderByPurchaseIdDesc(@Param("userId") String userId, Pageable pageable);

  Page<PurchaseProduct> findByDeliveryStatusOrderByPurchaseProductIdDesc(String deliveryStatus, Pageable pageable);

  Page<PurchaseProduct> findByUserIdOrderByPurchaseProductIdDesc(String userId, Pageable pageable);

  Page<PurchaseProduct> findByCancelReasonNotNullOrderByPurchaseProductIdDesc(Pageable pageable);

  Page<PurchaseProduct> findByUserIdAndCancelReasonIsNotNullOrderByPurchaseProductIdDesc(String userId, Pageable pageable);

  Page<PurchaseProduct> findByCancelReasonIsNullOrderByPurchaseProductIdDesc(Pageable pageable);

  int countByCancelReasonIsNull();
}

