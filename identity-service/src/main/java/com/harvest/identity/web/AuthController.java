package com.harvest.identity.web;

import com.harvest.common.security.UserContextResolver;
import com.harvest.identity.service.AuthService;
import com.harvest.identity.web.AuthDtos.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        return authService.register(request, response);
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/api/auth/google")
    public AuthResponse google(@RequestBody GoogleRequest request, HttpServletResponse response) {
        if (request.idToken() == null || request.idToken().isBlank()) {
            throw new IllegalArgumentException("idToken is required");
        }
        return authService.googleLogin(request.email(), request.name(), request.sub(), response);
    }

    @PostMapping("/api/auth/refresh")
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }
        if (refresh == null) {
            throw new IllegalArgumentException("Missing refresh token");
        }
        return authService.refresh(refresh, response);
    }

    @PostMapping("/api/auth/logout")
    public void logout(HttpServletResponse response) {
        authService.logout(response);
    }

    @GetMapping("/api/me")
    public UserResponse me(HttpServletRequest request) {
        String userId = UserContextResolver.fromHeaders(request).userId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        var user = authService.getMe(userId);
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getAvatar(), user.getRoles(), user.getAddresses());
    }

    @PatchMapping("/api/me")
    public UserResponse updateMe(HttpServletRequest request, @RequestBody ProfileRequest profileRequest) {
        String userId = UserContextResolver.fromHeaders(request).userId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Unauthorized");
        }
        var user = authService.updateMe(userId, profileRequest);
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getAvatar(), user.getRoles(), user.getAddresses());
    }

    @GetMapping("/api/admin/users")
    public List<UserResponse> users(HttpServletRequest request) {
        if (!UserContextResolver.fromHeaders(request).isAdmin()) {
            throw new IllegalArgumentException("Admin access required");
        }
        return authService.allUsers().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getName(), u.getAvatar(), u.getRoles(), u.getAddresses()))
                .toList();
    }
}