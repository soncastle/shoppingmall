package com.shoppingmall.order.controller;

import com.shoppingmall.order.domain.Purchase;
import com.shoppingmall.order.domain.PurchaseDelivery;
import com.shoppingmall.order.domain.PurchaseProduct;
import com.shoppingmall.order.dto.*;
import com.shoppingmall.order.repository.PurchaseDeliveryRepository;
import com.shoppingmall.order.repository.PurchaseProductRepository;
import com.shoppingmall.order.repository.PurchaseRepository;
import com.shoppingmall.order.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order/rest")
@RestController
public class PurchaseRestController {

  @PostMapping("/review")
  public ResponseEntity<?> uploadReview(
      @RequestParam("reviewText") String reviewText,
      @RequestParam("reviewImages") List<MultipartFile> reviewImages) {

    // 파일 저장 로직 (예제)
    for (MultipartFile file : reviewImages) {
      System.out.println("파일 이름: " + file.getOriginalFilename());
    }

    System.out.println("리뷰 내용: " + reviewText);

    return ResponseEntity.ok("리뷰 업로드 성공"); // 문자열 반환 (타입이 String)
  }

  @Autowired
  PurchaseProductRepository productRepo;

  @Autowired
  PurchaseRepository purchRepo;

  @Autowired
  PurchaseDeliveryRepository deliRepo;

  @Autowired
  PurchaseService service;

    @PostMapping("/order")
    public void order(@RequestBody List<PurchaseProductDto> dtos) {
      PurchaseDelivery delivery = new PurchaseDelivery();
      Purchase purchase = new Purchase();
      purchase.setCreateAt(now());

      purchase.setUserId(dtos.isEmpty() ? null : dtos.get(0).getUserId());
      purchRepo.save(purchase);
      delivery.setPurchase(purchase);
      deliRepo.save(delivery);
        dtos.forEach(dto -> {
        PurchaseProduct product = new PurchaseProduct();
        product.setProductId(dto.getProductId());
        product.setProductName(dto.getProductName());
        product.setOption(dto.getOption());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setUserId(dto.getUserId());
        product.setDeliveryStatus("배송준비중");
        product.setPurchase(purchase);
        productRepo.save(product);
      });
    }

    @PostMapping("/deliveryChange")
    public ResponseEntity<String> deliveryState(@RequestBody Map<String, String> request){
      String deliveryState = request.get("deliveryState");
      Long purchaseProductId = Long.parseLong(request.get("purchaseProductId"));
      System.out.println(deliveryState);
      System.out.println(purchaseProductId);
      String state = service.deliveryChange(deliveryState, purchaseProductId);
      System.out.println("sss" + state);

      return ResponseEntity.ok(state);
    }

    @PostMapping("/receiverChange")
    public ResponseEntity<String> receiverChange(@ModelAttribute DeliveryUpdateRequestDto dto){
      return ResponseEntity.ok(service.change(dto));
    }

  //물품 구입
  @PostMapping("/process")
  public ResponseEntity<?> processOrder(@ModelAttribute List<PurchaseProductDto> purchaseProductDtos,
                                        Authentication authentication) {
    try {
      String userId = authentication.getName();
      // 서비스 레이어로 주문 데이터 전달
      return ResponseEntity.ok().body("{\"success\":true}");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("{\"success\":false, \"message\":\"" + e.getMessage() + "\"}");
    }
  }
}
