package com.shoppingmall.user.model;

import com.shoppingmall.board.model.Board;
import com.shoppingmall.board.model.Comment;
import com.shoppingmall.cart.model.Cart;
import com.shoppingmall.order.domain.Coupon;
import com.shoppingmall.pet.model.Pet;
import com.shoppingmall.user.dto.UserResponseDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email", "accountType"})
})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String userId;

  @Column(length = 60, nullable = false)
  private String password;

  @Column(nullable = false)
  private String email;

  @Column(unique = true, nullable = false)
  private String nickname;

  @Column(nullable = false)
  private String accountType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRoleType role;

  private String question;
  private String answer;
  private String address;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Cart cart;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Coupon coupon;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private UserImg userImg;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Pet> pets;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Board> boards;

  @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL , orphanRemoval = true)
  private List<Comment> comments;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();

    if (this.cart == null) {
      this.cart = new Cart(); // User 생성 시 자동으로 Cart 생성
      this.cart.setUser(this); // Cart의 user도 설정
    }

    if (this.coupon == null) {
      this.coupon = new Coupon(); // User 생성 시 자동으로 Cart 생성
      this.coupon.setUser(this); // Cart의 user도 설정
    }

    if ( this.userImg == null){
      this.userImg = new UserImg();
      this.userImg.setUser(this);
      this.userImg.setUrl("/images/user-basic.jpg");
    }
  }




  public UserResponseDTO toDTO() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    return UserResponseDTO.builder()
        .userId(userId)
        .email(email)
        .nickname(nickname)
        .address(address)
        .accountType(accountType)
        .createdAt(createdAt.format(formatter))
        .role(role)
        .build();
  }
}


