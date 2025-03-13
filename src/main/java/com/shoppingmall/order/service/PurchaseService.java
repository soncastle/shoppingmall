package com.shoppingmall.order.service;

import com.shoppingmall.order.domain.PurchaseDelivery;
import com.shoppingmall.order.domain.PurchaseProduct;
import com.shoppingmall.order.domain.Purchase;
import com.shoppingmall.order.dto.*;
import com.shoppingmall.order.repository.PurchaseDeliveryRepository;
import com.shoppingmall.order.repository.PurchaseProductRepository;
import com.shoppingmall.order.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class PurchaseService {

	@Autowired
	PurchaseDeliveryRepository deliveryRepo;
	@Autowired
	PurchaseRepository purchaseRepo;
	@Autowired
	PurchaseProductRepository productRepo;

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
			products = productRepo.findByCancelAtIsNotNullAndUserIdOrderByPurchaseIdDesc(userId, pageable);
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
	public String deliveryChange(String deliveryState, Long purchaseProductId) {
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
		return deliveryState;
	}

	//주문 취소
	public String purchaseCancel(Long purchaseId) {
		List<Purchase> purchases = purchaseRepo.findByPurchaseId(purchaseId);
		List<PurchaseDelivery> deliveries = deliveryRepo.findByPurchaseId(purchaseId);
		List<PurchaseProduct> products = productRepo.findByPurchaseId(purchaseId);
		Purchase purchase = purchases.get(0);
		PurchaseDelivery delivery = deliveries.get(0);
		PurchaseProduct product = products.get(0);

		product.setCancelAt(LocalDateTime.now());
		delivery.setCancelAt(LocalDateTime.now());
		purchase.setCancelAt(LocalDateTime.now());

		deliveryRepo.save(delivery);
		productRepo.save(product);
		purchaseRepo.save(purchase);
		return "취소되었습니다";
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
			if(products.get(0).getUserId().equals(userId)){
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
	
//	주문 등록
	public void process(ProductAndDeliveryDto dtos) {
		Purchase purchase = new Purchase();
		System.out.println("dtos" + dtos);
		PurchaseDelivery delivery = dtos.getPurchaseDelivery().get(0);
		System.out.println("de" + delivery);
		System.out.println(dtos.getPurchaseDelivery().get(0).getDeliveryMessage());
		delivery.setPurchase(purchase);
		deliveryRepo.save(delivery);
		dtos.getPurchaseProduct().forEach(dto ->{
			PurchaseProduct product = new PurchaseProduct();
			product.setPurchase(purchase);
			product.setOption(dto.getOption());
			System.out.println(dto.getOption());
			product.setProductName(dto.getProductName());
			product.setProductId(dto.getProductId());
			product.setQuantity(dto.getQuantity());
			product.setPrice(dto.getPrice());
			product.setUserId(dto.getUserId());
			product.setProductName(dto.getProductName());
			product.setDeliveryStatus("배송준비중");
			product.setCreateAt(LocalDateTime.now());
		});
	}

	public void processOrder(CartToPuchaseDto cartDto, String userId) {
		// 주문 데이터 처리 로직 예제
		List<PurchaseProductDto> productList = cartDto.getPurchaseProductDtos();
		if (productList != null) {
		Purchase purchase = new Purchase();
		PurchaseProduct products = new PurchaseProduct();
		PurchaseDelivery delivery = new PurchaseDelivery();

		purchase.setUserId(userId);
		delivery.setReceiverAddr(cartDto.getReceiverAddr());
		delivery.setReceiverName(cartDto.getReceiverName());
		delivery.setReceiverPhone(cartDto.getReceiverPhone());
		delivery.setPurchase(purchase);

		// 주문상품 목록 처리
			int totalPrice = 0;
		for (PurchaseProductDto product : productList) {
			totalPrice += product.getPrice();
		}
			purchase.setTotalPrice(totalPrice);
			purchaseRepo.save(purchase);
			deliveryRepo.save(delivery);
			for (PurchaseProductDto product : productList) {
				products.setOption(product.getOption());
				products.setUserId(userId);
				products.setPurchase(purchase);
				products.setProductName(product.getProductName());
				products.setDeliveryStatus("배송준비중");
				products.setCreateAt(LocalDateTime.now());
				products.setQuantity(product.getQuantity());
				productRepo.save(products);
			}
		}
		System.out.println("주문이 처리되었습니다. 사용자 ID: " + cartDto.getUserId());
	}

}