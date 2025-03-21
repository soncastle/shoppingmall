package com.shoppingmall.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


  @GetMapping("/login")
  public String loginG(Authentication authentication) {
    if(authentication == null || authentication.getPrincipal() == null) {
      return "user/login";
    }
    return "redirect:/home";
  }
}
