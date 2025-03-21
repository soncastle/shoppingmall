package com.shoppingmall.config.security;

import com.shoppingmall.user.model.User;
import com.shoppingmall.user.repository.UserRepository;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

  private UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String userId) {
    User user = userRepository.findByUserId(userId);

    if (user == null) {
      throw new UsernameNotFoundException("User not found: " + userId);
    }

    return new CustomUserDetails(user);
  }
}
