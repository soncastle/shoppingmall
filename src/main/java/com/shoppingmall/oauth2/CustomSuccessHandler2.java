package com.shoppingmall.oauth2;

import com.shoppingmall.user.model.User;
import com.shoppingmall.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


@Component
public class CustomSuccessHandler2 extends SimpleUrlAuthenticationSuccessHandler {
  private final UserRepository userRepository;

  public CustomSuccessHandler2(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    HttpSession session = request.getSession();
    String userId = authentication.getName();

    // 사용자 정보 조회
    User user = userRepository.findByUserId(userId);
    if (user != null) {
      // 카트 수량 가져오기
      int cartCount = user.getCart().getUniqueItemCount();
      // 세션에 카트 수량 저장
      session.setAttribute("cartCount", cartCount);
    } else {
      session.setAttribute("cartCount", 0); // 사용자 정보가 없을 경우 0으로 설정
    }

    setDefaultTargetUrl("http://localhost:8080/home");
    // 기본 인증 성공 핸들러 호출
    super.onAuthenticationSuccess(request, response, authentication);

  }
}
