package com.cp.lab08sec1.demo.staff.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cp.lab08sec1.demo.staff.dto.LoginRequest;
import com.cp.lab08sec1.demo.staff.dto.UserRegistrationRequest;
import com.cp.lab08sec1.demo.staff.entity.StaffAccount;
import com.cp.lab08sec1.demo.staff.repository.StaffAccountRepository;

@Service
@Transactional
public class StaffAuthenticationService {

    private final StaffAccountRepository staffAccountRepository;

    public StaffAuthenticationService(StaffAccountRepository staffAccountRepository) {
        this.staffAccountRepository = staffAccountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<StaffAccount> authenticate(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Optional.empty();
        }
        String normalized = normalize(request.getUsername());
        return staffAccountRepository.findByUsernameIgnoreCase(normalized)
                .filter(account -> account.getPassword().equals(request.getPassword()));
    }

    @Transactional(readOnly = true)
    public List<StaffAccount> getAllUsers() {
        return staffAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
    }

    public boolean usernameExists(String username) {
        if (username == null) {
            return false;
        }
        return staffAccountRepository.existsByUsernameIgnoreCase(normalize(username));
    }

    public StaffAccount registerUser(UserRegistrationRequest request) {
        StaffAccount account = new StaffAccount();
        account.setUsername(request.getUsername().trim());
        account.setPassword(request.getPassword());
        account.setRole(request.getRole());
        account.setDisplayName(request.getDisplayName().trim());
        return staffAccountRepository.save(account);
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
