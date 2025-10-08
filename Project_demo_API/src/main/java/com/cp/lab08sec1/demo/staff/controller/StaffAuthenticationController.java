package com.cp.lab08sec1.demo.staff.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cp.lab08sec1.demo.staff.dto.LoginRequest;
import com.cp.lab08sec1.demo.staff.dto.LoginResponse;
import com.cp.lab08sec1.demo.staff.dto.UserRegistrationRequest;
import com.cp.lab08sec1.demo.staff.dto.UserRole;
import com.cp.lab08sec1.demo.staff.entity.StaffAccount;
import com.cp.lab08sec1.demo.staff.service.StaffAuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff")
@Validated
public class StaffAuthenticationController {

    private final StaffAuthenticationService authenticationService;

    public StaffAuthenticationController(StaffAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.authenticate(request)
                .map(account -> ResponseEntity.ok(buildResponse(account, "Login successful")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, null, null, "Invalid username or password")));
    }

    @PostMapping("/users")
    public ResponseEntity<LoginResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        if (authenticationService.usernameExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new LoginResponse(false, null, null, "Username already exists"));
        }
        StaffAccount account = authenticationService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(account, "User registered"));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        List<String> roles = Arrays.stream(UserRole.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/users")
    public ResponseEntity<List<LoginResponse>> getUsers() {
        List<LoginResponse> users = authenticationService.getAllUsers().stream()
                .map(account -> buildResponse(account, "User loaded"))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    private LoginResponse buildResponse(StaffAccount account, String message) {
        return new LoginResponse(true, account.getRole().name(), account.getDisplayName(), message);
    }
}