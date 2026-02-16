package com.example.project_management_tool.config;

import com.example.project_management_tool.entity.User;
import com.example.project_management_tool.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = "ROLE_" + u.getUserRole().name();

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
