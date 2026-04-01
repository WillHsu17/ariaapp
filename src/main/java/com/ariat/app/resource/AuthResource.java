package com.ariat.app.resource;

import com.ariat.app.service.AuthService;
import com.example.model.AuthRequest;
import com.example.model.AuthResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody AuthRequest authRequest) {
        if (!authRequest.getUsername().isEmpty() && !authRequest.getPassword().isEmpty()) {
            return authService.signup(authRequest.getUsername(), authRequest.getPassword());
        }
        return null;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        if (!authRequest.getUsername().isEmpty() && !authRequest.getPassword().isEmpty()) {
            return authService.login(authRequest.getUsername(), authRequest.getPassword());
        }
        return null;
    }
}