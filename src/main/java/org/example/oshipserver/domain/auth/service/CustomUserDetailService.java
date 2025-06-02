package org.example.oshipserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.entity.CustomUserDetail;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return userRepository.findById(Long.parseLong(username))
                       .map(CustomUserDetail::new)
                       .orElseThrow(()->new ApiException("user not found", ErrorType.NOT_FOUND));
        }
}
