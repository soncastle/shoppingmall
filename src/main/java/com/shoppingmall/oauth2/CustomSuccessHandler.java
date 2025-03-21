package com.shoppingmall.oauth2;

import com.shoppingmall.config.security.CustomUserDetails;
import com.shoppingmall.oauth2.model.CustomOAuth2User;
import com.shoppingmall.user.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        String userId = null;
        String email = null;
        String role = null;
        String account = null;
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            userId = customOAuth2User.getName();
            email = customOAuth2User.getEmail();
            account = customOAuth2User.getAccountType();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            role = authorities.iterator().next().getAuthority();
            System.out.println("account type : " + account);

        } else {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = customUserDetails.getUsername();
            email = customUserDetails.getEmail();
            account = customUserDetails.getAccountType();
            System.out.println(account);
            role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        }

        String token = jwtUtil.createJwt(userId, email, account, role, 3600L);
        response.addCookie(createCookie("Authorization", token));
        response.sendRedirect("http://localhost:8080/home");


    }


    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
