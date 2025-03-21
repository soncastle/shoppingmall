package com.shoppingmall.config.security;


import com.shoppingmall.oauth2.CustomSuccessHandler;
import com.shoppingmall.oauth2.CustomSuccessHandler2;
import com.shoppingmall.oauth2.service.CustomOAuth2UserService;
import com.shoppingmall.user.jwt.JWTUtil;
import com.shoppingmall.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.support.MultipartFilter;
import java.util.Arrays;
import java.util.Collections;

import static org.springframework.security.web.util.matcher.RegexRequestMatcher.regexMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private CustomUserDetailsService userDetailsService;
  private CustomAuthenticationFailureHandler failureHandler;
  private CustomOAuth2UserService customOAuth2UserService;
  private final CustomSuccessHandler2 successHandler;
  private final JWTUtil jwtUtil;

  public SecurityConfig(CustomUserDetailsService userDetailsService,
                        CustomAuthenticationFailureHandler failureHandler,
                        CustomOAuth2UserService customOAuth2UserService,
                        CustomSuccessHandler2 successHandler,
                        JWTUtil jwtUtil) {
      this.userDetailsService = userDetailsService;
      this.failureHandler = failureHandler;
      this.customOAuth2UserService = customOAuth2UserService;
      this.successHandler = successHandler;
      this.jwtUtil = jwtUtil;
  }



    @Bean
    public MultipartFilter multipartFilter() {
        MultipartFilter multipartFilter = new MultipartFilter();
        multipartFilter.setMultipartResolverBeanName("multipartResolver");
        return multipartFilter;
    }


  @Bean
  public HttpFirewall allowSemicolonHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true); // 세미콜론 허용
        return firewall;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        // 기본 HTTP 인증 비활성화
        http.httpBasic(auth -> auth.disable());

        // CSRF 보호 비활성화
        http.csrf(csrf -> csrf.disable());



        http
        	.rememberMe(remember -> remember
            .key("uniqueAndSecret") // 보안 키 설정
            .tokenValiditySeconds(7 * 24 * 60 * 60) // 7일 동안 유지
            .rememberMeParameter("remember-me") // 체크박스 파라미터 (기본값: remember-me)
        );

        // URL 기반 접근 권한 설정
        http
                .authorizeHttpRequests(auth -> auth
                // 공통 페이지 - 인증 없이 접근 가능
                .requestMatchers("/", "/home", "/index.html").permitAll()

                // 로그인 관련 페이지
                .requestMatchers(regexMatcher("/login.*")).permitAll()

                // 사용자 관련 페이지
                .requestMatchers("/users/agree", "/users", "/users/find/**", "/users/addr").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/find/**", "/api/users/check").permitAll()

                // 팀원별 기능 페이지 - 모두 접근 가능 설정
                // ex) /cart/** -> cart 부터 아래의 하위 경로 허용
                // ex) /cart    -> cart 경로 허용

                .requestMatchers("/cart/**").permitAll()// 수민님
                .requestMatchers("/cart/cart/**").permitAll()
                .requestMatchers("/product/**" , "/products/**").permitAll() // 진호님
                .requestMatchers("/board/**").permitAll()                                          // 준서님
                .requestMatchers("/order/**").permitAll()// 성호님

                // 정적 리소스 접근 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                // Swagger 문서 접근 허용
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // 그 외 모든 요청은 인증 필요
                .anyRequest().permitAll()
        );

        // 폼 로그인 설정
        http.formLogin(form -> form
                .loginPage("/login")                  // 로그인 페이지 경로
                .loginProcessingUrl("/login/process") // 로그인 처리 경로
                .successHandler(successHandler)
                .failureHandler(failureHandler)       // 로그인 실패 시 핸들러
        );

        // OAuth2 소셜 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")                  // 소셜 로그인 페이지 경로
                .successHandler(successHandler)      // 소셜 로그인 성공 시 리다이렉트 경로
                .userInfoEndpoint(userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(customOAuth2UserService))
        );

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutSuccessUrl("/home")            // 로그아웃 성공 시 리다이렉트 경로
                .permitAll()
        );

        // 세션 관리 설정
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // 필요시 세션 생성
                .maximumSessions(1)                                       // 최대 세션 수 제한
                .expiredUrl("/login")                                     // 세션 만료 시 리다이렉트 경로
        );

        // CORS 설정 (프론트엔드 개발용)
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));



        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:5173");  // React 앱의 URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.addAllowedHeader("*");  // 모든 헤더 허용
        configuration.setAllowCredentials(true);  // 자격 증명 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 CORS 설정 적용

        return source;
    }
}
