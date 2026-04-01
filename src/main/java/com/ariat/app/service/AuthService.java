package com.ariat.app.service;

import com.ariat.app.entity.User;
import com.ariat.app.service.dao.UserDao;
import com.ariat.app.util.JwtUtil;
import com.example.model.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserDao userDao,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(String username, String rawPassword) {

        // 1. Find user
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 3. Generate JWT (delegated to JwtUtil)
        return new AuthResponse(OffsetDateTime.now(), jwtUtil.generateToken(username));
    }

    public AuthResponse signup(String username, String password) {

        if (userDao.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String encoded = passwordEncoder.encode(password);

        User user = new User(username, encoded, "USER");

        userDao.save(user);
        return new AuthResponse(OffsetDateTime.now(), jwtUtil.generateToken(username));


    }
}