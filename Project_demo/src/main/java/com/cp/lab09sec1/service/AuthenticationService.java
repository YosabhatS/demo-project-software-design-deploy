package com.cp.lab09sec1.service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.cp.lab09sec1.StaffApiProperties;
import com.cp.lab09sec1.dto.LoginRequest;
import com.cp.lab09sec1.dto.LoginResponse;
import com.cp.lab09sec1.dto.UserRegistrationRequest;
import com.cp.lab09sec1.entity.UserAccount;
import com.cp.lab09sec1.entity.UserRole;

@Service
public class AuthenticationService {

    private static final ParameterizedTypeReference<LoginResponse> LOGIN_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<String>> ROLE_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<LoginResponse>> USER_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient staffApiWebClient;
    private final StaffApiProperties properties;

    public AuthenticationService(WebClient staffApiWebClient, StaffApiProperties properties) {
        this.staffApiWebClient = staffApiWebClient;
        this.properties = properties;
    }

    public RemoteResponse<LoginResponse> authenticate(LoginRequest request) {
        return exchange(HttpMethod.POST, "/login", request, LOGIN_RESPONSE_TYPE);
    }

    public RemoteResponse<LoginResponse> registerUser(UserRegistrationRequest request) {
        return exchange(HttpMethod.POST, "/users", request, LOGIN_RESPONSE_TYPE);
    }

    public RemoteResponse<List<String>> fetchRoles() {
        return exchange(HttpMethod.GET, "/roles", null, ROLE_LIST_TYPE);
    }

    public RemoteResponse<List<LoginResponse>> fetchUsers() {
        return exchange(HttpMethod.GET, "/users", null, USER_LIST_TYPE);
    }

    private <T> RemoteResponse<T> exchange(HttpMethod method, String path, Object body,
            ParameterizedTypeReference<T> typeReference) {
        try {
            WebClient.RequestBodySpec requestSpec = staffApiWebClient.method(method).uri(path);
            ResponseEntity<T> entity;
            if (body != null) {
                entity = requestSpec
                        .bodyValue(body)
                        .exchangeToMono(response -> response.toEntity(typeReference))
                        .block(resolveTimeout());
            } else {
                entity = requestSpec
                        .exchangeToMono(response -> response.toEntity(typeReference))
                        .block(resolveTimeout());
            }
            if (entity == null) {
                throw new RemoteServiceException("Authentication API did not return a response");
            }
            HttpStatus status = resolveStatus(entity.getStatusCode());
            return new RemoteResponse<>(status, entity.getBody());
        } catch (WebClientRequestException ex) {
            throw new RemoteServiceException("Unable to reach authentication API", ex);
        } catch (RuntimeException ex) {
            throw new RemoteServiceException("Failed to process authentication API response", ex);
        }
    }

    private Duration resolveTimeout() {
        Duration timeout = properties.getTimeout();
        return timeout != null ? timeout : Duration.ofSeconds(5);
    }

    private HttpStatus resolveStatus(HttpStatusCode statusCode) {
        if (statusCode == null) {
            return HttpStatus.OK;
        }
        HttpStatus resolved = HttpStatus.resolve(statusCode.value());
        return resolved != null ? resolved : HttpStatus.OK;
    }
}