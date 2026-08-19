package com.hinchmart.service;

import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.AccountStatus;
import com.hinchmart.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or phone: " + identifier));

        boolean enabled = user.getStatus() == AccountStatus.ACTIVE;
        boolean accountNonLocked = user.getStatus() != AccountStatus.SUSPENDED;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled,
                true,
                true,
                accountNonLocked,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
