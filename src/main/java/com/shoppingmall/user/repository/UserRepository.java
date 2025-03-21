package com.shoppingmall.user.repository;

import com.shoppingmall.user.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;



@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  User findByEmailAndAccountType(String email, String accountType);

  User findByUserIdAndAccountType(String userId, String accountType);


  User findByEmail(String email);

  User findByUserId(String userId);

  User findByUserIdAndEmail(String userId, String email);

  User findByQuestionAndAnswer(String question, String answer);

  Boolean existsByUserId(String userId);

  Boolean existsByEmail(String email);

  Boolean existsByNickname(String nickname);

  Boolean existsByEmailAndAccountType(String email, String accountType);

  User findByUserIdAndPassword(String userId , String password);


}
