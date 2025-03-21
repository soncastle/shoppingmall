package com.shoppingmall.user.controller;


import com.shoppingmall.board.repository.BoardRepository;
import com.shoppingmall.oauth2.model.CustomOAuth2User;
import com.shoppingmall.user.dto.*;
import com.shoppingmall.user.exception.FieldErrorsException;
import com.shoppingmall.user.model.User;
import com.shoppingmall.user.repository.UserRepository;
import com.shoppingmall.user.service.EmailService;
import com.shoppingmall.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/users")
public class UserApiController {

  private final UserRepository userRepository;
  private final UserService userService;
  private final EmailService emailService;
  private final BoardRepository boardRepository;


  public UserApiController(
      UserService userService,
      UserRepository userRepository,
      EmailService emailService,
      BoardRepository boardRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.emailService = emailService;
    this.boardRepository = boardRepository;
  }

  @GetMapping
  public ResponseEntity<ApiResponse<?>> getUserInfo(Authentication authentication) {
    if(authentication == null || authentication.getPrincipal() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String userId = authentication.getName();
    UserResponseDTO userResponseDTO = userService.getUser(userId);

    return ResponseEntity.ok(ApiResponse.success(userResponseDTO));
  }



  // 회원가입 필드별 유효성 검사
  @Operation(summary = "중복된 값이 존재하는지 확인하는 컨트롤러", description = "fieldName 과 fieldValue 를 정보로 요청")
  @PostMapping(value = "/check", produces = "application/json")
  public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkDuplicate(
      @RequestBody UserRequestDTO.Check dto ) {

    boolean result = userService.checkDuplicate(dto.getFieldName(), dto.getFieldValue());
    Map<String, Boolean> resultMap = Map.of("result", result);
    return ResponseEntity.ok(ApiResponse.success(resultMap));
  }

  // 회원가입 요청 및 최종 검사
  @PostMapping
  @Operation(
      summary = "회원가입 요청을 처리하는 컨트롤러",
      description = "usrId , password , email , nickname , question , answer , address 정보로 요청")
  public ResponseEntity<ApiResponse<?>> registerUser(
      @Valid @RequestBody UserRequestDTO.Create userDTO, BindingResult bindingResult) {
    // response 객체 생성
    Map<String, String> response = new HashMap<>();
    if (bindingResult.hasErrors()) {
      filedErrorsHandler(bindingResult);
    }

    userService.registerUser(userDTO);
    response.put("userId", userDTO.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @GetMapping("/me/profile/MyPageTopInfo")
  public ResponseEntity<ApiResponse<?>> myPageTopInfo(Authentication authentication) {
    UserResponseDTO.MypageInfo dto = userService.getMyPageTopInfo(authentication.getName());
    return ResponseEntity.ok(ApiResponse.success(dto));
  }

  @GetMapping("/me/profile")
  @Operation(
      summary = "회원 정보 조회",
      description =
          "인증된 사용자의 정보를 받아와 출력 / 현재 시큐리티 permitAll 때문에 인증이 안된 사용자는 에러페이지 , 인증이 된 사용자만 마이페이지 ")
  public ResponseEntity<?> profileG(Authentication authentication) {
    if (authentication.isAuthenticated()) {
      User user = userRepository.findByUserId(authentication.getName());
      return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user.toDTO()));
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("인증되지 않은 사용자"));
  }

  @PostMapping("/me/profile")
  public ResponseEntity<?> UpdateProfile(
      @RequestParam("nickname") String nickname ,
      @RequestParam(value = "profilePhotoFile", required = false) MultipartFile profilePhotoFile , Authentication authentication) {

    try {
      String userId = authentication.getName();
      System.out.println("프로필 업데이트 요청 - 사용자 ID: " + userId);

      // 서비스 메소드 호출
      String imageUrl = userService.userProfileUpdate(userId, nickname, profilePhotoFile);
      System.out.println("프로필 업데이트 완료 - 이미지 URL: " + imageUrl);

      // 응답 데이터 준비
      Map<String, Object> data = new HashMap<>();
      data.put("url", imageUrl != null ? imageUrl : "");
      data.put("nickname", nickname);

      return ResponseEntity.ok(ApiResponse.success(data));
    } catch (Exception e) {
      // 오류 발생 시 로그 출력 후 예외 전파
      System.err.println("프로필 업데이트 오류: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  @PatchMapping("/me/profile")
  @Operation(summary = "회원 정보 수정(업데이트)", description = "요청시 nickname , email , address 를 정보로 요청")
  public ResponseEntity<ApiResponse<?>> UpdateUser(
      @Valid @RequestBody UserRequestDTO.Update dto, BindingResult bindingResult ,Authentication authentication) {
    if (bindingResult.hasErrors()) {
      filedErrorsHandler(bindingResult);
    }
    String userId = authentication.getName();
    userService.updateUser(dto , userId);
    return ResponseEntity.ok().body(ApiResponse.success());
  }

  @PostMapping("/find/id")
  @Operation(summary = "회원 아이디 찾기", description = "요청시 question 과 answer 정보로 요청")
  public ResponseEntity<ApiResponse<?>> findId(
      @RequestBody UserRequestDTO.FindID dto, Model model) {
    String userId = userService.findUserId(dto);
    return ResponseEntity.ok(ApiResponse.success(userId));
  }

  @PostMapping("/find/pass")
  @ResponseBody
  @Operation(summary = "회원 비밀번호 찾기", description = "요청시 userId 와 email 정보로 요청")
  public ResponseEntity<ApiResponse<?>> findPassword(@RequestBody UserRequestDTO.FindPass dto)
      throws MessagingException {
    Map<String, String> response = new HashMap<>();
    emailService.findPassword(dto.getUserId(), dto.getEmail());
    response.put("message", "이메일로 임시 비밀번호가 전송 되었습니다.<br> 새로운 비밀번호로 로그인해주세요.");
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @DeleteMapping
  public ResponseEntity<?> DeleteUser(
      @RequestBody UserRequestDTO.Delete dto,
      Authentication authentication,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (authentication.getPrincipal() instanceof CustomOAuth2User) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    if (!authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String userId = authentication.getName();
    userService.deleteUser(userId, dto.getPassword());

    // 회원 탈퇴후 세션 만료 및 쿠키 삭제 명시적 처리
    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    logoutHandler.setClearAuthentication(true);
    logoutHandler.setInvalidateHttpSession(true);
    logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("JSESSIONID")) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
          break;
        }
      }
    }

    return ResponseEntity.ok().body(ApiResponse.success());
  }

  @GetMapping("/me/activities/{type}")
  public ResponseEntity<ApiResponse<?>> getActivities(
      @PathVariable String type, Authentication authentication) {
    String userId = authentication.getName();
    return ResponseEntity.ok(ApiResponse.success(userService.getActivities(type, userId)));
  }

  // 에러 핸들링 메소드
  public void filedErrorsHandler(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    throw new FieldErrorsException(errors);
  }
}
