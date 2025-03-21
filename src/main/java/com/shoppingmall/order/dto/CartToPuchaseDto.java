package com.shoppingmall.order.dto;

import com.shoppingmall.order.domain.PurchaseProduct;
import com.shoppingmall.product.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartToPuchaseDto {
  private String userId;
  private String userPhone;
  private String receiverName;
  private String receiverPhone;
  private String receiverAddr;
  private String receiverAddrDetail;
  private String deliveryMessage;
  private String customDeliveryRequest;
  private String coupon;
  private String paymentMethod;
  private String cardCompany;
  private String totalPrice;

  private List<PurchaseReadyDto> product;  // 여러 개의 상품을 받을 리스트

}
