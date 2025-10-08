package com.cp.lab09sec1.controller;

import java.util.Arrays;
import java.util.Collections;
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

import com.cp.lab09sec1.dto.LoginRequest;
import com.cp.lab09sec1.dto.LoginResponse;
import com.cp.lab09sec1.dto.UserRegistrationRequest;
import com.cp.lab09sec1.entity.UserAccount;
import com.cp.lab09sec1.entity.UserRole;
import com.cp.lab09sec1.service.AuthenticationService;
import com.cp.lab09sec1.service.RemoteResponse;
import com.cp.lab09sec1.service.RemoteServiceException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthenticationController {

    private static final LoginResponse SERVICE_UNAVAILABLE_RESPONSE = new LoginResponse(false, null, null,
            "Authentication service is currently unavailable");

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            RemoteResponse<LoginResponse> remote = authenticationService.authenticate(request);
            LoginResponse body = ensureBody(remote.body(), remote.status(),
                    "Authentication service returned no data");
            return ResponseEntity.status(remote.status()).body(body);
        } catch (RemoteServiceException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(SERVICE_UNAVAILABLE_RESPONSE);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<LoginResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            RemoteResponse<LoginResponse> remote = authenticationService.registerUser(request);
            LoginResponse body = ensureBody(remote.body(), remote.status(),
                    "Authentication service returned no data");
            return ResponseEntity.status(remote.status()).body(body);
        } catch (RemoteServiceException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(SERVICE_UNAVAILABLE_RESPONSE);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        try {
            RemoteResponse<List<String>> remote = authenticationService.fetchRoles();
            List<String> roles = remote.body() != null ? remote.body() : Collections.emptyList();
            return ResponseEntity.status(remote.status()).body(roles);
        } catch (RemoteServiceException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<LoginResponse>> getUsers() {
        try {
            RemoteResponse<List<LoginResponse>> remote = authenticationService.fetchUsers();
            List<LoginResponse> users = remote.body() != null ? remote.body() : Collections.emptyList();
            return ResponseEntity.status(remote.status()).body(users);
        } catch (RemoteServiceException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
        }
    }

    private LoginResponse ensureBody(LoginResponse body, HttpStatus status, String fallbackMessage) {
        if (body != null) {
            return body;
        }
        boolean success = status != null && status.is2xxSuccessful();
        return new LoginResponse(success, null, null, fallbackMessage);
    }
}