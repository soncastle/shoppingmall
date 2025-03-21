package com.shoppingmall.order.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PurchaseReturnsDto {
  private Long purchaseProductId;
  private String returnsContent;
  private String cancelReason;
  // 여러 장의 이미지를 받기 위해 배열로 선언 (form의 input name="exchangeImage"와 매핑)
  private MultipartFile[] returnsImage;
}
