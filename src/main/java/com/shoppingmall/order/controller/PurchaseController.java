package com.shoppingmall.order.controller;
import com.shoppingmall.order.domain.Purchase;
import com.shoppingmall.order.domain.PurchaseReturns;
import com.shoppingmall.order.dto.*;
import com.shoppingmall.order.service.PurchaseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/order")
@Controller
@RequiredArgsConstructor
public class PurchaseController {

	final PurchaseService service;

	//주문 페이지 내용 세션에 저장
@PostMapping("/orders")
public ResponseEntity<Map<String, String>> cartToPurchase(@RequestBody List<PurchaseReadyDto> dtos,
																		 HttpSession session) {
session.setAttribute("orderDto", dtos);
	Map<String, String> response = new HashMap<>();
	response.put("result", "good");
	System.out.println(session.getAttributeNames().toString());

	return ResponseEntity.ok(response);
}

	//세션에 저장된 내용 받아와서 주문 완료 페이지로 반환
@GetMapping("/cartToOrder")
public String orderPage(Model model, HttpSession session,
						Authentication authentication) {
	System.out.println(session.getAttribute("orderDto"));
	System.out.println("bbbbbbbbbb" + authentication.getName());
	// 세션에 저장된 주문 데이터를 불러와 모델에 담기
	Object sessionOrderObj = session.getAttribute("orderDto");
	String userId = authentication.getName();
	List<PurchaseReadyDto> orderData = service.orderPage(sessionOrderObj);

	final int[] totalPrice = {0};
	orderData.forEach(data -> {
		String priceStr = data.getPrice().replace(",", "");
		int price = Integer.parseInt(priceStr);
		totalPrice[0] += price;
		data.setPrice(String.valueOf(price));
		String imageUrl = data.getImageUrl();
		if (imageUrl != null) {
			data.setImageUrl(imageUrl);
		}
	});
	model.addAttribute("coupon", service.choiceCoupon(userId));
	model.addAttribute("product", orderData);
	model.addAttribute("userId", userId);
	model.addAttribute("totalPrice", totalPrice[0]);
	return "order/cartToOrder";
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

	//관리자용주문번호 기준 주문검색
	@GetMapping("/admin/orders/{purchaseId}")
	public String orderByPurchaseId(@PathVariable Long purchaseId, Model model){
		PurchaseAllDto purchaseAllDto = service.getOrderDetails(purchaseId);
		model.addAttribute("delivery", purchaseAllDto.getPurchaseDelivery());
		model.addAttribute("purchase", purchaseAllDto.getPurchase());
		model.addAttribute("item", purchaseAllDto.getPurchaseProduct());
		model.addAttribute("dto", purchaseAllDto);
			return "order/adminOrderOne";
}

//전체 회원 리스트 주문 검색(전체별, 취소별, 주문요청별)
@GetMapping("/admin/orderList")
public String orderAll(@RequestParam(name = "purchaseState", required = false, defaultValue = "all") String purchaseState,
						 @RequestParam(name = "page", defaultValue = "0") int page,
						 @RequestParam(name = "size", defaultValue = "5") int size,
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

	//관리자용 배송상태 변경
	@PostMapping("/admin/deliveryChange")
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

//사용자별 취소 리스트 검색
@PostMapping("/cancelAll")
@ResponseBody
public ResponseEntity<?> cancelAll(@RequestParam(name = "purchaseId") Long purchaseId,
								   Authentication authentication) {
	String userId = authentication.getName();
	service.cancelAll(purchaseId, userId);
	return ResponseEntity.ok().body(Map.of("message", "결제가 취소되었습니다."));
}

	//userId 기준 주문 검색
//	@GetMapping("/orders/userId")
//	public String orderListByUserId(
//									Authentication authentication,
//									@RequestParam(name = "page", defaultValue = "0") int page,
//									@RequestParam(name = "size", defaultValue = "5") int size,
//									@RequestParam(name = "purchaseState", required = false, defaultValue = "all") String purchaseState,
//									@RequestParam(name = "admin", required = false, defaultValue = "user") String admin,
//									Model model) {
//			Pageable pageable = PageRequest.of(page, size);
//			String userId = authentication.getName();
//			PurchasePageDto dto = service.orderListByUserId(userId, purchaseState, pageable);
//			if(dto.getPurchase()==null){
//				model.addAttribute("purchase", new Purchase());
//			}else{
//				model.addAttribute("purchase", dto.getPurchase());
//			}
//			model.addAttribute("delivery", dto.getPurchaseDelivery());
//			model.addAttribute("item", dto.getPurchaseProduct());
//			model.addAttribute("currentPage", page);
//			model.addAttribute("totalPages", dto.getPurchase().getTotalPages());
//			model.addAttribute("pageSize", size);
//		if (admin.equals("admin")) {
//			return "order/adminOrderByUserId";
//		}
//		//취소일 경우
//		if(purchaseState.equals("cancel")){
//			return "order/purchaseCancel";
//			// 일반 유저의 경우
//		}
//		return "order/orderListByUserIdByProductId";
//	}
	@GetMapping("/orders/userId")
	public String orderListByUserId(
									Authentication authentication,
									@RequestParam(name = "purchaseState", required = false, defaultValue = "all") String purchaseState,
									@PageableDefault(page = 0, size = 5)  Pageable pageable,
									Model model) {
			String userId = authentication.getName();
			model.addAttribute("item", service.userOrderList(userId, pageable, purchaseState));
		return "order/userOrderList";
	}

	@GetMapping("/admin/orders/userId")
	public String adminOrderListByUserId(
									@RequestParam(name = "userId", required = false, defaultValue = "id") String id,
									@RequestParam(name = "page", defaultValue = "0") int page,
									@RequestParam(name = "size", defaultValue = "5") int size,
									@RequestParam(name = "purchaseState", required = false, defaultValue = "all") String purchaseState,
									Authentication authentication,
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
			return "order/adminOrderByUserId";
	}

	//주문번호 기준 상세보기
	@GetMapping("/one")
	public String purchaseNumber(@RequestParam(name = "purchaseProductId") Long purchaseProductId,
								Model model){
	 model.addAttribute("result", service.purchaseNumber(purchaseProductId));
	return "order/orderResultOne";
	}

	//소비자용 배송중 리스트
	@GetMapping("/onDelivery")
	public String onDelivery(Authentication authentication,
							 @PageableDefault(page = 0, size = 5) Pageable pageable,
							Model model){
		String userId = authentication.getName();
		model.addAttribute("item", service.onDelivery(userId, pageable));
		return "order/orderListOnDelivery";
	}

	//환불 요청
	@PostMapping("/returns")
	public  String purchaseReturns(@ModelAttribute PurchaseReturnsDto dto,
									 Authentication authentication,
								   @PageableDefault(page = 0, size = 5) Pageable pageable) {
		System.out.println("gg");
		service.createExchangeRequest(dto, authentication.getName(), pageable);
	return "redirect:/order/orders/userId";
	}
	
	//관리자용 환불 리스트
@GetMapping("/admin/returnsList")
public String returnsList(Model model, @PageableDefault(page = 0, size = 5) Pageable pageable) {
	System.out.println(132215253);
	model.addAttribute("item", service.getAllReturns(pageable));
	System.out.println(service.getAllReturns(pageable).getTotalPages());
	Page<PurchaseReturns> purchaseReturns = service.getAllReturns(pageable);
	return "order/adminPurchaseCancel";
}
	//쿠폰 정보 확인
	@GetMapping("/coupon")
	public String coupon(Model model,
						 Authentication authentication,
						 @PageableDefault(page = 0, size = 5) Pageable pageable){
	String userId = authentication.getName();
	model.addAttribute("coupons", service.getCoupon(pageable, userId));
	return "order/coupon";
	}

// 관리자용 배송 요청 리스트
@GetMapping("/admin/orders/request")
public String adminPurchaseRequest(Model model,  @PageableDefault(page = 0, size = 5)  Pageable pageable){
model.addAttribute("item", service.adminPurchaseRequest(pageable));
return "order/adminPurchaseRequest";
}
}
