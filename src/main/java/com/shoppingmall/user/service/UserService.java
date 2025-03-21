package com.shoppingmall.user.service;

import com.shoppingmall.board.dto.BoardResponseDTO;
import com.shoppingmall.board.model.Board;
import com.shoppingmall.board.repository.BoardRepository;
import com.shoppingmall.order.domain.CouponList;
import com.shoppingmall.order.repository.CouponListRepository;
import com.shoppingmall.order.repository.CouponRepository;
import com.shoppingmall.user.dto.UserRequestDTO;
import com.shoppingmall.user.dto.UserResponseDTO;
import com.shoppingmall.user.exception.DuplicateException;
import com.shoppingmall.user.model.User;
import com.shoppingmall.user.model.UserImg;
import com.shoppingmall.user.repository.UserImgRepository;
import com.shoppingmall.user.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;

import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

  private final UserImgRepository userImgRepository;
  private UserRepository userRepository;
  private BoardRepository boardRepository;
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  public UserService(
      UserRepository userRepository,
      BoardRepository boardRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder, UserImgRepository userImgRepository) {
    this.userRepository = userRepository;
    this.boardRepository = boardRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.userImgRepository = userImgRepository;
  }

  // 개별 필드 검사
  public boolean checkDuplicate(String fieldName, String fieldValue) {
    // 해당 fieldName 에 맞는 코드 연결 후 fieldValue 검색
    // 프론트엔드는 boolean 값을 받아서 중복이 있는지 없는지 판단 후에 사용자에게 알림.
    boolean isDuplicate =
        switch (fieldName) {
          case "userId" -> userRepository.existsByUserId(fieldValue);
          case "email" -> userRepository.existsByEmailAndAccountType(fieldValue, "NORMAL");
          case "nickname" -> userRepository.existsByNickname(fieldValue);
          // 해당하는 3가지 필드 중 아무것도 아니라면 예외 처리
          default -> throw new IllegalStateException("Unexpected value: " + fieldName);
        };

    // boolean 값 반환
    return isDuplicate;
  }

  // 회원가입 요청시 최종 중복 검사
  public void checkDuplicate(UserRequestDTO.Create userDTO) {
    // error 를 담을 객체 생성
    Map<String, String> errors = new HashMap<>();
    // 아이디가 이미 있다면 해당 메시지 put
    if (userRepository.existsByUserId(userDTO.getUserId())) {
      errors.put("userId", "이미 사용 중인 아이디입니다.");
    }
    // 이메일이 이미 있다면 해당 메시지 put
    if (userRepository.existsByEmailAndAccountType(userDTO.getEmail(), "NORMAL")) {
      errors.put("email", "이미 사용 중인 이메일입니다.");
    }
    // 닉네임이 이미 있다면 해당 메시지 put
    if (userRepository.existsByNickname(userDTO.getNickname())) {
      errors.put("nickname", "이미 사용 중인 닉네임입니다.");
    }
    // errors 객체에 put 한 메시지가 하나라도 있다면 예외 처리
    if (!errors.isEmpty()) {
      throw new DuplicateException(errors);
    }
  }

  // 유저 생성
  @Transactional
  public void registerUser(UserRequestDTO.Create userDTO) {
    // userDTO 의 유저 정보 userId , email , nickname 중복 검사
    // 해당 메서드는 UserRequestDTO 를 인수로 받는 checkDuplicate 메서드 (위에 명시)
    checkDuplicate(userDTO); // 2. 중복 검사
    // userDTO 의 유저 정보 비밀번호 암호화
    userDTO.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));

    // userDTO 를 엔티티로 변환
    User user = userDTO.toEntity();
    user.setAccountType("NORMAL");
    // 유저 저장
    userRepository.save(user);
  }

  // 유저 조회
  public UserResponseDTO getUser(String userId) {
    // 해당하는 유저 검색
    User user = userRepository.findByUserId(userId);
    // 유저가 없다면 예외처리
    if (user == null) {
      throw new UsernameNotFoundException("해당하는 정보로 찾지 못했습니다.");
    }
    // 유저 정보 반환
    return user.toDTO();
  }

  public UserResponseDTO.MypageInfo getMyPageTopInfo(String userId) {
    User user = userRepository.findByUserId(userId);
    if (user == null) {
      throw new UsernameNotFoundException("해당하는 정보로 찾지 못했습니다.");
    }
    System.out.println("가져오는중");
    String nickname = user.getNickname();
    int quantity = user.getCart().getUniqueItemCount();
    String imgUrl = user.getUserImg().getUrl();
    int couponCount = 3;

    return UserResponseDTO.MypageInfo.builder()
            .nickname(nickname)
            .couponCount(couponCount)
            .cartQuantity(quantity)
            .url(imgUrl)
            .build();
  }

  // 유저 수정
  @Transactional
  public void updateUser(UserRequestDTO.Update userDTO , String userId) {
    Map<String, String> errors = new HashMap<>();
    // 해당하는 유저 검색
    User user = userRepository.findByUserId(userId);

    // db의 유저 정보와 수정한 유저의 정보가 같지 않고 중복된 이메일이 있을때
    if (!user.getEmail().equals(userDTO.getEmail())
        && userRepository.existsByEmail(userDTO.getEmail())) {
      errors.put("email", "이미 사용 중인 이메일입니다.");
    }
    // db의 유저 정보와 수정한 유저의 정보가 같지 않고 중복된 아이디가 있을때
    if (!user.getNickname().equals(userDTO.getNickname())
        && userRepository.existsByNickname(userDTO.getNickname())) {
      errors.put("nickname", "이미 사용 중인 닉네임입니다.");
    }
    // 에러가 하나라도 있으면 예외 처리
    if (!errors.isEmpty()) {
      throw new DuplicateException(errors);
    }

    // 유저 정보 갱신
    user.setNickname(userDTO.getNickname());
    user.setEmail(userDTO.getEmail());
    user.setAddress(userDTO.getAddress());
    userRepository.save(user);
  }

  // 유저 삭제
  @Transactional
  public void deleteUser(String userId, String password) {

    User user = userRepository.findByUserId(userId);
    if (user == null) {
      throw new UsernameNotFoundException("회원이 존재 하지 않습니다.");
    }

    if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
      throw new IllegalStateException("비밀번호가 일치 하지 않습니다.");
    }

    userRepository.delete(user);
  }

  // 질문과 답변에 맞는 유저 찾는 기능
  public String findUserId(UserRequestDTO.FindID dto) {
    User user = userRepository.findByQuestionAndAnswer(dto.getQuestion(), dto.getAnswer());
    if (user == null) {
      throw new UsernameNotFoundException("입력한 정보로 조회된 아이디가 없습니다.");
    }
    return user.getUserId();
  }

  // 유저의 닉네임과 프로필사진 을 업데이트 하는 기능
  public String userProfileUpdate(String userId, String nickname , MultipartFile file) {
    User user = userRepository.findByUserId(userId);
    if (user == null) {
      throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
    }

    String imageUrl = null;

    // 중복 닉네임 검사
    if (userRepository.existsByNickname(nickname) && !nickname.equals(user.getNickname())) {
      throw new DuplicateException();
    }

    // 파일이 있을 경우 저장
    if (file != null && !file.isEmpty()) {
      // 지정 경로에 파일 저장
      imageUrl = saveProfileImage(file);
      System.out.println("이미지 URL: " + imageUrl);

      UserImg existingUserImg = userImgRepository.findByUser(user);
      System.out.println(existingUserImg.getUrl());
      if (existingUserImg != null) {
        // 기존 이미지가 있을 경우 업데이트
        String url = existingUserImg.getUrl();
        deleteProfileImage(url);
        existingUserImg.setUrl(imageUrl);
        userImgRepository.save(existingUserImg);
      } else {
        // 기존 이미지가 없을 경우 새 UserImg 저장
        UserImg userImg = new UserImg();
        userImg.setUrl(imageUrl);
        userImg.setUser(user);
        userImgRepository.save(userImg);
        user.setUserImg(userImg);
      }
    }

    // 닉네임 업데이트
    user.setNickname(nickname);
    userRepository.save(user);

    return imageUrl;

  }

  private void deleteProfileImage(String imageUrl) {
      String basePath =  new File("src/main/resources/static").getAbsolutePath();

      if(imageUrl != null || !imageUrl.isEmpty()) {
        File oldFile = new File(basePath + imageUrl);

        if(oldFile.exists()) {
          boolean delete = oldFile.delete();
          System.out.println("기존이미지 삭제 여부 " + delete);
        }

      }
  }

  private String saveProfileImage(MultipartFile file) {
      if (file == null || file.isEmpty()) {
        return null;
      }

      // 지정된 외부 경로 사용
      String basePath = new File("src/main/resources/static/images").getAbsolutePath();
      String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
      String filePath = basePath + File.separator + fileName;
      File destinationFile = new File(filePath);

      System.out.println("저장된 대표 이미지 경로: " + filePath);
      // 파일 저장
      try{
        file.transferTo(destinationFile);
        return "/images/" + fileName;

      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("파일 저장에 실패했습니다: " + e.getMessage());
      }
  }

  // 현재 사용자의 회원 정보를 가져오는 기능
  public User getCurrentUser(Authentication authentication) {
    User user = userRepository.findByUserId(authentication.getName());
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }
    return user;
  }

  // 작성한 게시물 목록 , 좋아요한 게시물 목록 , 댓글 쓴 게시물 목록을 가져오는 기능
  public Map<String, Object> getActivities(String type, String userId) {

    Map<String, Object> response = new HashMap<>();
    List<BoardResponseDTO> boardsDtos = null;
    User user = userRepository.findByUserId(userId);
    if (type.equals("boards")) {
      boardsDtos = user.getBoards().stream().map(Board::toDTO).toList();

      response.put("boards", boardsDtos);

    } else if (type.equals("comments")) {

      boardsDtos =
          user.getComments().stream()
              .map(comment -> comment.getBoard().toDTO())
              .distinct()
              .toList();

      response.put("comments", boardsDtos);

    } else if (type.equals("likes")) {

      boardsDtos =
          boardRepository.findAll().stream()
              .filter(
                  board -> {
                    Set<Long> likes = board.getLikeContain();
                    return likes != null && likes.contains(user.getId());
                  })
              .map(Board::toDTO)
              .toList();

      response.put("likes", boardsDtos);
    }
    return response;
  }
}
