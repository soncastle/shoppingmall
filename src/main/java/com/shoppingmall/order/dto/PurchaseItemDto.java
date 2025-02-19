package com.shoppingmall.order.dto;

import com.shoppingmall.order.domain.PurchaseItem;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PurchaseItemDto {

  private Long orderItemId; // 주문 상품 ID (PK)
  private Long productId; // 상품 ID
  private String productName; // 상품명
  private String option; // 상품 옵션
  private int quantity; // 수량
  private int price; // 가격
  private int totalPrice; // 해당 상품 총 가격

  @Builder
  public PurchaseItemDto(Long orderItemId, Long productId, String productName, String option, int quantity, int price, int totalPrice) {
    this.orderItemId = orderItemId;
    this.productId = productId;
    this.productName = productName;
    this.option = option;
    this.quantity = quantity;
    this.price = price;
    this.totalPrice = totalPrice;
  }

  public static PurchaseItemDto fromEntity(PurchaseItem item) {
    return PurchaseItemDto.builder()
        .orderItemId(item.getPurchaseItemId()) // 필드명 수정
        .productId(item.getProductId())
        .productName(item.getProductName())
        .option(item.getOption())
        .quantity(item.getQuantity())
        .price(item.getPrice())
        .totalPrice(item.getTotalPrice())
        .build();
  }
}
