package com.shoppingmall.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "메인 홈페이지 API", description = "컨트롤러에 대한 설명입니다.")
@Controller
public class HomeController {

  @GetMapping("/home")
  public String home() {
    return "home";
  }

  @GetMapping("/header")
  public String header() {
    return "headerFragment/header";
  }

}
