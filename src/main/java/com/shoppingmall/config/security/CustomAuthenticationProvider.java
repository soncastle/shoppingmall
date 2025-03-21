package com.shoppingmall.config.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class CustomAuthenticationProvider implements AuthenticationProvider {

  private CustomUserDetailsService userDetailsService;
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService , BCryptPasswordEncoder bCryptPasswordEncoder) {
      this.userDetailsService = userDetailsService;
      this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String userId = authentication.getName();
    String password = authentication.getCredentials().toString();
    CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(userId);

    System.out.println(userId);
    System.out.println(password);
    if(user == null) {
      throw new UsernameNotFoundException(" 사용자를 찾을 수 없습니다."  + userId);
    }

    if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("비밀번호가 틀립니다.");
    }
    return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
