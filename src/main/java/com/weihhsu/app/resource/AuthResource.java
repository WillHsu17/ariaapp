package com.weihhsu.app.resource;


import com.weihhsu.app.entity.User;
import com.weihhsu.app.service.dao.UserDao;
import com.weihhsu.app.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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

        if (userDao.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String encoded = passwordEncoder.encode(password);

        User user = new User(username, encoded, "USER");

        userDao.save(user);

        return jwtUtil.generateToken(username);
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