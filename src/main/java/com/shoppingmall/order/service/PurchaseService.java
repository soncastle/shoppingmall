package com.shoppingmall.order.service;

import com.shoppingmall.cart.repository.CartItemRepository;
import com.shoppingmall.order.domain.*;
import com.shoppingmall.order.dto.*;
import com.shoppingmall.order.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PurchaseService {

	private final PurchaseDeliveryRepository deliveryRepo;
	private final PurchaseRepository purchaseRepo;
	private final PurchaseProductRepository productRepo;
	private final PurchaseReturnsRepository returnsRepo;
	private final CartItemRepository cartRepo;
	private final CouponListRepository couponListRepo;
	private final CouponRepository couponRepo;


	public String order(Purchase purchase, PurchaseDelivery delivery, PurchaseProduct item, String receiveDetailAddr) {

		purchase.setCreateAt(LocalDateTime.now());
		item.setCreateAt(LocalDateTime.now());
		delivery.setUserId(purchase.getUserId());
		item.setUserId(purchase.getUserId());
		purchaseRepo.save(purchase);
		productRepo.save(item);
		delivery.setReceiverAddr(delivery.getReceiverAddr() + " " + receiveDetailAddr);
		delivery.setPurchase(purchase);
		delivery.setDeliveryStatus("배송준비중");
		deliveryRepo.save(delivery);

		item.setPurchase(purchase);
		productRepo.save(item);

		return "주문해주셔서 감사합니다";
	}

	//전체 주문 / 취소 검색
	public PurchasePageDto purchaseList(String purchasestate, Pageable pageable) {
		Page<Purchase> purchases;
		Page<PurchaseDelivery> deliveries;
		Page<PurchaseProduct> products;

		if (purchasestate.equals("all")) {
			purchases = purchaseRepo.findAllOrderByPurchaseIdDesc(pageable);
			deliveries = deliveryRepo.findAllOrderByPurchaseIdDesc(pageable);
			products = productRepo.findAllOrderByPurchaseIdDesc(pageable);
		} else if (purchasestate.equals("cancel")) {
			purchases = purchaseRepo.findByCancelAtIsNotNullOrderByPurchaseIdDesc(pageable);
			deliveries = deliveryRepo.findByCancelAtIsNotNullOrderByPurchaseIdDesc(pageable);
			products = productRepo.findByCancelAtIsNotNullOrderByPurchaseIdDesc(pageable);
		} else {
			purchases = purchaseRepo.findByCancelAtIsNullOrderByPurchaseIdDesc(pageable);
			deliveries = deliveryRepo.findByCancelAtIsNullOrderByPurchaseIdDesc(pageable);
			products = productRepo.findByCancelAtIsNullOrderByPurchaseIdDesc(pageable);
		}

		return PurchasePageDto.builder()
				.purchase(purchases)
				.purchaseDelivery(deliveries)
				.purchaseProduct(products)
				.build();

	}

	//주문번호로 상세 주문 검색
	public PurchaseAllDto getOrderDetails(Long purchaseId) {

		List<Purchase> purchases = purchaseRepo.findByPurchaseId(purchaseId);
		List<PurchaseDelivery> deliveries = deliveryRepo.findByPurchaseId(purchaseId);
		List<PurchaseProduct> products = productRepo.findByPurchaseId(purchaseId);

		return PurchaseAllDto.builder()
				.purchase(purchases)
				.purchaseDelivery(deliveries)
				.purchaseProduct(products)
				.build();
	}

	//userId별 주문 검색
	public PurchasePageDto orderListByUserId(String userId, String purchaseState, Pageable pageable) {
		Page<Purchase> purchases;
		Page<PurchaseDelivery> deliveries;
		Page<PurchaseProduct> products;

		if (purchaseState.equals("all")) {
			purchases = purchaseRepo.findByUserIdOrderByPurchaseIdDesc(userId, pageable);
			deliveries = deliveryRepo.findByUserIdOrderByPurchaseIdDesc(userId, pageable);
			products = productRepo.findByUserIdOrderByPurchaseIdDesc(userId, pageable);
		} else if (purchaseState.equals("cancel")) {
			purchases = purchaseRepo.findByCancelAtIsNotNullAndUserIdOrderByPurchaseIdDesc(userId, pageable);
			deliveries = deliveryRepo.findByCancelAtIsNotNullAndUserIdOrderByPurchaseIdDesc(userId, pageable);
			products = productRepo.findByCancelAtIsNotNullAndUserIdOrderByPurchaseProductIdDesc(userId, pageable);
			System.out.println(products.get());
		} else {
			purchases = purchaseRepo.findByCancelAtIsNullAndUserIdOrderByPurchaseIdDesc(userId, pageable);
			deliveries = deliveryRepo.findByCancelAtIsNullAndUserIdOrderByPurchaseIdDesc(userId, pageable);
			products = productRepo.findByCancelAtIsNullAndUserIdOrderByPurchaseIdDesc(userId, pageable);
		}
		return PurchasePageDto.builder()
				.purchase(purchases)
				.purchaseDelivery(deliveries)
				.purchaseProduct(products)
				.build();
	}

	//배송 상태 변경
	public Long deliveryChange(String deliveryState, Long purchaseProductId) {
		if (deliveryState.equals("onDelivery")) {
			deliveryState = "배송중";
		} else if (deliveryState.equals("finDelivery")) {
			deliveryState = "배송완료";
		} else {
			deliveryState = "배송취소";
		}
		System.out.println("SER" + "안녕" + deliveryState);
		PurchaseProduct product = productRepo.findByPurchaseProductId(purchaseProductId).get(0);
		product.setDeliveryStatus(deliveryState);
		productRepo.save(product);

		return product.getPurchase().getPurchaseId();
	}

	//주문 취소
	public void purchaseCancel(Long purchaseProductId) {
		PurchaseProduct product = productRepo.findById(purchaseProductId).orElse(null);
		product.setCancelAt(LocalDateTime.now());
		product.setCancelReason("취소");
		productRepo.save(product);
	}

	//주문 전체 취소
	public void purchaseCancelAll(Long purchaseId) {
		List<PurchaseProduct> products = productRepo.findByPurchaseId(purchaseId);
		products.forEach(product -> {
			product.setCancelAt(LocalDateTime.now());
			product.setCancelReason("취소");
			productRepo.save(product);
		});
	}

	//배송정보 수정 관련
	public PurchaseDeliveryDto receiverChange(DeliveryChangeDto dto, String state) {
		List<PurchaseDelivery> receiver = deliveryRepo.findByPurchaseId(dto.getPurchaseId());
		PurchaseDelivery receiverChanse = receiver.get(0);
		if (state.equals("change")) {
			receiverChanse.setReceiverName(dto.getReceiverName());
			receiverChanse.setReceiverPhone(dto.getReceiverPhone());
			if (dto.getDetailAddr() == null) {
				dto.setDetailAddr("");
			}
			receiverChanse.setReceiverAddr(dto.getReceiverAddr() + "  " + dto.getDetailAddr());
			receiverChanse.setDeliveryMessage(dto.getDeliveryMessage());
			deliveryRepo.save(receiverChanse);
		}
		return PurchaseDeliveryDto.builder()
				.deliveryId(receiverChanse.getDeliveryId())
				.receiverName(receiverChanse.getReceiverName())
				.receiverAddr(receiverChanse.getReceiverAddr())
				.receiverPhone(receiverChanse.getReceiverPhone())
				.deliveryMessage(receiverChanse.getDeliveryMessage())
				.build();
	}

	public ProductAndDeliveryDto purchaseNumber(String userId, Long purchaseProductId) {
		List<PurchaseProduct> products = productRepo.findByPurchaseProductId(purchaseProductId);
		if (products.get(0).getUserId().equals(userId)) {
			List<PurchaseDelivery> deliveries = deliveryRepo.findByPurchaseId(products.get(0).getPurchase().getPurchaseId());

			return ProductAndDeliveryDto.builder()
					.purchaseDelivery(deliveries)
					.purchaseProduct(products)
					.build();
		}
		return ProductAndDeliveryDto.builder()
				.message("false")
				.build();

	}

	public String change(DeliveryUpdateRequestDto dto) {
		List<PurchaseDelivery> receiver = deliveryRepo.findByPurchaseId(dto.getPurchaseId());
		PurchaseDelivery receiverChanse = receiver.get(0);
		receiverChanse.setReceiverName(dto.getReceiverName());
		receiverChanse.setReceiverPhone(dto.getReceiverPhone());
		if (dto.getReceiverAddrDetail() == null) {
			dto.setReceiverAddrDetail("");
		}
		receiverChanse.setReceiverAddr(dto.getReceiverAddr() + " " + dto.getReceiverAddrDetail());
		receiverChanse.setDeliveryMessage(dto.getDeliveryMessage());
		deliveryRepo.save(receiverChanse);
		String message = "수정 되었습니다.";
		return message;
	}

	//주문 등록
	public Long processOrder(CartToPuchaseDto cartDto, String userId) {
		Purchase purchase = new Purchase();
		PurchaseDelivery delivery = new PurchaseDelivery();
		List<PurchaseReadyDto> productList = new ArrayList<>();

		cartDto.getProduct().forEach(dto -> {
			productList.add(dto);
		});

		Double totalPurchase = Double.parseDouble(cartDto.getTotalPrice());
		int totalPrice = totalPurchase.intValue();
		purchase.setTotalPrice(totalPrice);
		purchase.setCreateAt(LocalDateTime.now());
		purchase.setUserId(userId);

		delivery.setReceiverAddr(cartDto.getReceiverAddr() + " " + cartDto.getReceiverAddrDetail());
		delivery.setReceiverName(cartDto.getReceiverName());
		delivery.setReceiverPhone(cartDto.getReceiverPhone());
		if (cartDto.getDeliveryMessage().equals("door")) {
			delivery.setDeliveryMessage("문 앞에 놓아주세요");
		} else if (cartDto.getDeliveryMessage().equals("guard")) {
			delivery.setDeliveryMessage("경비실에 맡겨주세요");
		} else if (cartDto.getDeliveryMessage().equals("contact")) {
			delivery.setDeliveryMessage("배송 완료 전 전 연락 바랍니다");
		} else {
			delivery.setDeliveryMessage(cartDto.getDeliveryMessage());
		}

		// 주문상품 목록 처리
		List<PurchaseProduct> purchaseProduct = new ArrayList<>();
		productList.forEach(product -> {
			PurchaseProduct purchaseItem = new PurchaseProduct();

			purchaseItem.setOption(product.getOption());
			purchaseItem.setUserId(userId);
			purchaseItem.setPurchase(purchase);
			purchaseItem.setProductName(product.getProductName());
			purchaseItem.setDeliveryStatus("배송준비중");
			purchaseItem.setCreateAt(LocalDateTime.now());
			purchaseItem.setQuantity(Integer.parseInt(product.getQuantity()));
			purchaseProduct.add(purchaseItem);
		});

		purchase.setPurchaseProduct(purchaseProduct);
		purchaseRepo.save(purchase);
		delivery.setPurchase(purchase);
		deliveryRepo.save(delivery);

		final int[] onePrice = {0};
		onePrice[0] = 0;

		//개별 상품 가격의 총 가격
		productList.forEach(product -> {
			Double price = Double.parseDouble(product.getPrice());
			onePrice[0] = onePrice[0] + price.intValue();
		});

		final int[] sumPrice = {0};
		final int[] totalPurchasePrice = {0};

		//쿠폰 적용된 총 가격
		sumPrice[0] = totalPrice;

		//적용된 할인
		totalPurchasePrice[0] = onePrice[0] - totalPrice;

		productList.forEach(product -> {
			PurchaseProduct products = new PurchaseProduct();
			Double resultPrice = Double.parseDouble(product.getPrice());
			int price = resultPrice.intValue();

			//할인이 있을 경우
			if (totalPurchasePrice[0] != 0) {
				totalPurchasePrice[0] = totalPurchasePrice[0] - price;
				if (totalPurchasePrice[0] < 0) {
					products.setPrice(-totalPurchasePrice[0]);
					totalPurchasePrice[0] = 0;
				} else {
					products.setPrice(0);
				}
			} else {
				products.setPrice(price);
			}

			long productId = Long.parseLong(product.getProductId());
			cartRepo.deleteByProductIds(productId);
			products.setProductId(productId);
			products.setOption(product.getOption());
			products.setUserId(userId);
			products.setPurchase(purchase);
			products.setProductName(product.getProductName());

			products.setDeliveryStatus("배송준비중");
			products.setCreateAt(LocalDateTime.now());
			products.setImageUrl(product.getImageUrl());
			products.setQuantity(Integer.parseInt(product.getQuantity()));
			productRepo.save(products);
		});

		System.out.println("주문이 처리되었습니다. 사용자 ID: " + userId);
		return purchase.getPurchaseId();
	}

	public List<PurchaseProduct> onDelivery(String userId) {
		String deliveryStatus = "배송중";
		List<PurchaseProduct> products = productRepo.findByDeliveryStatusAndUserIdOrderByPurchaseProductIdDesc(deliveryStatus, userId);
		products.forEach(dto -> {
			System.out.println(dto.getProductName());
		});
		return products;
	}

	public void purchaseConform(String id) {
		Double purchaseProductId = Double.parseDouble(id);
		List<PurchaseProduct> products = productRepo.findByPurchaseProductId(purchaseProductId.longValue());
		PurchaseProduct product = products.get(0);
		product.setPurchaseConform("구매확정");
		productRepo.save(product);
	}

	public void createExchangeRequest(PurchaseReturnsDto dto, String userId) {
		PurchaseReturns returns = new PurchaseReturns();
		StringBuilder imagePaths = new StringBuilder();
		PurchaseProduct product = productRepo.findByPurchaseProductId(dto.getPurchaseProductId()).get(0);
		returns.setPurchaseProduct(product);
		returns.setCreateAt(LocalDateTime.now());
		product.setCancelAt(LocalDateTime.now());
		if (dto.getCancelReason().equals("cancel")) {
			product.setCancelReason("취소");
		} else {
			product.setCancelReason("반품");
		}
		productRepo.save(product);
		returns.setReturnsContent(dto.getReturnsContent());
		returns.setUserId(userId);

		MultipartFile[] files = dto.getReturnsImage();
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					try {
						// 파일명 생성 (현재 시간 + 원본 파일명)
						String originalFilename = file.getOriginalFilename();
						String fileNameOnly = Paths.get(originalFilename).getFileName().toString();
						String fileName = System.currentTimeMillis() + "_" + fileNameOnly;

						// 실제 저장될 경로 (파일 시스템)
						Path uploadPath = Paths.get("src/main/resources/static/images/order/returns");
						if (!Files.exists(uploadPath)) {
							Files.createDirectories(uploadPath);
						}

						// 파일 저장
						Path filePath = uploadPath.resolve(fileName);
						Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

						// 웹 접근용 경로는 스프링 부트의 정적 리소스 매핑에 따른 경로를 직접 지정
						String webPath = "/images/order/returns/" + fileName;
						imagePaths.append(webPath).append(",");

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 마지막 콤마 제거
		if (imagePaths.length() > 0) {
			imagePaths.deleteCharAt(imagePaths.length() - 1);
		}
		returns.setReturnsImagePaths(imagePaths.toString());
		returnsRepo.save(returns);
	}

	//취소/횐불/교환요청 확인
	public Page<PurchaseReturns> getAllReturns(Pageable pageable) {
		return returnsRepo.findAll(pageable);
	}

	public Page<CouponDto> getCoupon(Pageable pageable, String userId){
		Page<Coupon> coupons = couponRepo.findByUserId(userId, pageable);
		List<CouponDto> dtoList = coupons.getContent().stream()
				.map(coupon -> {
					CouponDto dto = new CouponDto();
					dto.setCouponId(coupon.getId());
					dto.setUserId(coupon.getUser().getUserId());
					dto.setCouponList(coupon.getCouponList());
					return dto;
				})
				.collect(Collectors.toList());

		Page<CouponDto> couponDtos = new PageImpl<>(
				dtoList,
				pageable,
				coupons.getTotalElements()
		);

		return couponDtos;

	}
}


