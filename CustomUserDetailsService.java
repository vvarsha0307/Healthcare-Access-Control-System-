package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.model.Permission;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new UsernameNotFoundException("User is not active");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    // 🔐 ROLE + PERMISSION HANDLING
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {

        Set<GrantedAuthority> authorities = new HashSet<>();

        authorities.add(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );

        for (Permission permission : user.getRole().getPermissions()) {
            authorities.add(
                    new SimpleGrantedAuthority(permission.getName())
            );
        }

        return authorities;
    }

    // 🔥 ADD THIS METHOD (VERY IMPORTANT FOR SHIFT SYSTEM)
    public User loadUserEntityByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}