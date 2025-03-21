package com.shoppingmall.order.controller;

import com.shoppingmall.order.domain.Purchase;
import com.shoppingmall.order.domain.PurchaseReview;
import com.shoppingmall.order.dto.*;
import com.shoppingmall.order.repository.PurchaseReviewRepository;
import com.shoppingmall.order.service.PurchaseService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/order")
@Controller
@RequiredArgsConstructor
public class PurchaseController {

@PostMapping("/orders")
public ResponseEntity<Map<String, String>> cartToPurchase(@RequestBody List<PurchaseReadyDto> dtos,
																		 HttpSession session) {
session.setAttribute("orderDto", dtos);
	Map<String, String> response = new HashMap<>();
	response.put("result", "good");
	return ResponseEntity.ok(response);
}

@GetMapping("/cartToOrder")
public String orderPage(Model model, HttpSession session,
						Authentication authentication) {
	// 세션에 저장된 주문 데이터를 불러와 모델에 담기
	Object sessionOrderObj = session.getAttribute("orderDto");
	List<PurchaseReadyDto> orderData = new ArrayList<>();
	if (sessionOrderObj instanceof List<?>) {
		for (Object obj : (List<?>) sessionOrderObj) {
			if (obj instanceof PurchaseReadyDto) {
				orderData.add((PurchaseReadyDto) obj);
			}
		}
	}
	final double[] totalPrice = {0.0};
	orderData.forEach(data -> {
		double price = Double.parseDouble(data.getPrice());
		int eachPrice = (int) price;
		totalPrice[0] += eachPrice;
		data.setPrice(String.valueOf(eachPrice));
		String imageUrl = data.getImageUrl();
		if (imageUrl != null) {
			imageUrl = imageUrl.replace("http://localhost:8080/", "")
					.replace("[", "")
					.replace("]", "");
			data.setImageUrl(imageUrl);
		}
	});
	String userId = authentication.getName();
	model.addAttribute("product", orderData);
	model.addAttribute("userId", userId);
	model.addAttribute("totalPrice", totalPrice[0]);
	// 추가 주문자 정보, 총 결제 금액 등도 모델에 담아서 Thymeleaf 템플릿으로 전달
	return "order/cartToOrder";
}

		@GetMapping("/purchase")
public String purchase(){ return "order/purchaseReady";}

@GetMapping("/review")
public String review(){return "order/review";}

final PurchaseService service;

final PurchaseReviewRepository purchaseReviewRepository;

@PostMapping("/purchase/review")
public void reviews(@RequestParam(name = "productId")Long productId,
											@RequestParam(name = "comment") String comment,
											@RequestParam(name = "rating") int rating,
											@RequestParam(name = "reviewImages") List<MultipartFile> reviewImages,
											Model model){
	System.out.println(productId);
	System.out.println(comment);
	System.out.println(reviewImages);
	List<String> imagePaths = new ArrayList<>();

	try {
		for (MultipartFile file : reviewImages) {
			String fileName = file.getOriginalFilename();
			String filePath = "/images/" + fileName;  // 이미지 경로

			// 실제 파일 저장은 하지 않지만, 경로는 DB에 저장
			imagePaths.add(filePath);
		}

		// Review 객체 생성
		PurchaseReview review = new PurchaseReview();
		review.setComment(comment);
		review.setImagePaths(imagePaths);
		review.setRating(rating);

		// 리뷰 저장 (DB에 저장)
		purchaseReviewRepository.save(review);

		// 모델에 경로 넘기기
		model.addAttribute("imagePaths", imagePaths);
	} catch (IOException e) {
		e.printStackTrace();
	}
}

	//주문번호 기준 주문검색
@GetMapping("/orders/{purchaseId}")
public String orderByPurchaseId(@PathVariable Long purchaseId,
								@RequestParam(name="admin", required = false, defaultValue = "user") String admin, Model model){
	PurchaseAllDto purchaseAllDto = service.getOrderDetails(purchaseId);
	model.addAttribute("delivery", purchaseAllDto.getPurchaseDelivery());
	model.addAttribute("purchase", purchaseAllDto.getPurchase());
	model.addAttribute("item", purchaseAllDto.getPurchaseProduct());
	model.addAttribute("dto", purchaseAllDto);
	if(admin.equals("admin")){
		return "order/adminOrderOne";
	}
	return "order/orderResult";
}

//전체 회원 리스트 주문 검색(전체별, 취소별, 주문요청별)
@GetMapping("/admin/orderList")
public String orderAll(@RequestParam(name = "purchaseState", required = false, defaultValue = "all") String purchaseState,
						 @RequestParam(name = "page", defaultValue = "0") int page,
						 @RequestParam(name = "size", defaultValue = "8") int size,
						 Model model){

	Pageable pageable = PageRequest.of(page, size);
	PurchasePageDto purchasePageDto = service.purchaseList(purchaseState, pageable);
	// 페이지 정보를 모델에 추가
	if(purchasePageDto.getPurchase()==null){
		model.addAttribute("purchase", new Purchase());
	}else{
		model.addAttribute("purchase", purchasePageDto.getPurchase());
	}
	model.addAttribute("delivery", purchasePageDto.getPurchaseDelivery());
	model.addAttribute("item", purchasePageDto.getPurchaseProduct());
	model.addAttribute("currentPage", page); // 현재 페이지
	model.addAttribute("totalPages", purchasePageDto.getPurchase().getTotalPages()); // 전체 페이지 수
	model.addAttribute("pageSize", size);
return "order/adminOrder";
	}

	//배송상태 변경
	@PostMapping("/admin/deiveryChange")
	public String deliveryChange(@RequestParam("deliveryState")String deliveryState,
															 RedirectAttributes redirectAttributes,
								 @RequestParam("purchaseProductId")Long purchaseProductId,
										 Model model){
		Long purchaseId = service.deliveryChange(deliveryState, purchaseProductId);
		redirectAttributes.addAttribute("purchaseId", purchaseId);
		return "redirect:/order/orders/{purchaseId}?admin=" + "admin";
	}

	//주문 취소
	@PostMapping("/orders/{purchaseProductId}")
	public String orderCancel(@PathVariable Long purchaseProductId){
		service.purchaseCancel(purchaseProductId);
	return "redirect:/order/one?purchaseProductId=" + purchaseProductId;
	}

	@PostMapping("/orders/cancelAll")
	public String purchaseCancelAll(@RequestParam(name = "purchaseId") Long purchaseId){
		service.purchaseCancel(purchaseId);
	return "redirect:/order/orders/{purchaseId}=" + purchaseId;
	}

	//userId 기준 주문 검색
	@GetMapping("/orders/userId")
	public String orderListByUserId(
									Authentication authentication,
									@RequestParam(name = "page", defaultValue = "0") int page,
									@RequestParam(name = "size", defaultValue = "8") int size,
									@RequestParam(name = "purchaseState", required = false, defaultValue = "all") String purchaseState,
									@RequestParam(name = "admin", required = false, defaultValue = "user") String admin,
									Model model) {
		Pageable pageable = PageRequest.of(page, size);

		String userId = authentication.getName();

			PurchasePageDto dto = service.orderListByUserId(userId, purchaseState, pageable);
			if(dto.getPurchase()==null){
				model.addAttribute("purchase", new Purchase());
			}else{
				model.addAttribute("purchase", dto.getPurchase());
			}
			model.addAttribute("delivery", dto.getPurchaseDelivery());
			model.addAttribute("item", dto.getPurchaseProduct());
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", dto.getPurchase().getTotalPages());
			model.addAttribute("pageSize", size);
		if (admin.equals("admin")) {
			return "order/adminOrderByUserId";
		}
		//취소일 경우
		if(purchaseState.equals("cancel")){
			return "order/purchaseCancel";
		}
		// 일반 유저의 경우
		return "order/orderListByUserIdByProductId";
	}

	//수령인 정보 변경 / state가 false일 경우 경로이동
	@GetMapping("/orders/receiver")
	public String receiverChange(@ModelAttribute DeliveryChangeDto deliveryChangeDto,
								 @RequestParam(name="state", required = false, defaultValue = "show") String state,
								 Model model){
	model.addAttribute("delivery", service.receiverChange(deliveryChangeDto, state));
		if(state.equals("show")){
			model.addAttribute("purchaseProductId", deliveryChangeDto.getPurchaseProductId());
			return "order/receiverChange";
			}
	return "redirect:/order";
	}
	
	//주문번호 기준 상세보기
	@GetMapping("/one")
	public String purchaseNumber(Authentication authentication,
								@RequestParam(name = "purchaseProductId") Long purchaseProductId,
								Model model){
	String userId = authentication.getName();
	 ProductAndDeliveryDto productAndDeliveryDto = service.purchaseNumber(userId, purchaseProductId);
	 if(productAndDeliveryDto.equals("false")){
		 model.addAttribute("message", "오류가 발생하여 다시 실행 바랍니다.");
		 return "redirect:order";
	 }
	 model.addAttribute("result", productAndDeliveryDto);
	return "order/orderResultOne";
	}
	@GetMapping("/onDelivery")
	public String onDelivery(Authentication authentication,
														Model model){
		String userId = authentication.getName();
		model.addAttribute("item", service.onDelivery(userId));
		return "order/orderListByUserIdByProductId";
	}

	//환불 요청
	@PostMapping("/returns")
	public  String purchaseReturns(@ModelAttribute PurchaseReturnsDto dto,
																 Authentication authentication,
																 Model model) {
	 service.createExchangeRequest(dto, authentication.getName());
	return "redirect:/order/orders/userId";
	}
	
	//환불 리스트
	@GetMapping("/returnsList")
	public String exchangeList(Model model,
							   Pageable pageable) {
		model.addAttribute("returns", service.getAllReturns(pageable));
		return "order/returns";
	}

	@GetMapping("/coupon")
	public String coupon(Model model,
						 Authentication authentication,
						 Pageable pageable){
		System.out.println("ggggg");
	String userId = authentication.getName();
	model.addAttribute("coupons", service.getCoupon(pageable, userId));
	return "order/coupon";
	}
}