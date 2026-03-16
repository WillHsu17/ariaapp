package com.weihhsu.app.resource;

import com.example.model.AuthResponse;
import com.example.model.LoginRequest;
import com.example.model.SignupRequest;
import com.weihhsu.app.entity.User;
import com.weihhsu.app.service.dao.UserDao;
import com.weihhsu.app.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@RequestMapping("/auth")
public class AuthResource {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResource(UserDao userDao,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String password) {

        String encoded = passwordEncoder.encode(password);

        User user = new User(username, encoded, "USER");

        userDao.save(user);

        return "User created";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password) {

        User user = userDao.findByUsername(username)
                .orElseThrow();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(username);
    }
}