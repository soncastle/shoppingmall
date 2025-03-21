package com.shoppingmall.config;

import java.io.File;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String projectRoot = System.getProperty("user.dir");
    String uploadPath = projectRoot + "/src/main/resources/static/images";

    File dir = new File(uploadPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    registry.addResourceHandler("/images/**")
        .addResourceLocations("file:" + uploadPath + "/")
        .setCachePeriod(0);

    System.out.println("업로드 경로 설정: " + uploadPath);
  }
}
