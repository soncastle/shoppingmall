package com.shoppingmall.oauth2.model;

import com.shoppingmall.user.dto.UserResponseDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserResponseDTO userResponseDTO;

    public CustomOAuth2User(UserResponseDTO userResponseDTO) {
        this.userResponseDTO = userResponseDTO;
        System.out.println(
            "Created CustomOAuth2User with email: 여긴 생성자 " + userResponseDTO.getEmail());

    }


    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userResponseDTO.getRole().name();
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return userResponseDTO.getUserId();
    }

    public String getEmail() {
        return userResponseDTO.getEmail();
    }

    public String getAccountType() {
        return userResponseDTO.getAccountType();
    }


    public UserResponseDTO getOAuth2Response() {
        return userResponseDTO;
    }
}
