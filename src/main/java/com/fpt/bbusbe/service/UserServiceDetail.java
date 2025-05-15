package com.fpt.bbusbe.service;

import com.fpt.bbusbe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceDetail {

    private final UserRepository userRepository;

    public UserDetailsService UserServiceDetail() {
        return userRepository::findByPhone;
    }
}
