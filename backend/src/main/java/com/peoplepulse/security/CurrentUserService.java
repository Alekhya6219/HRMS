package com.peoplepulse.security;

import com.peoplepulse.model.User;
import com.peoplepulse.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User account not found"));
    }

    public Long getCurrentEmployeeId() {
        User user = getCurrentUser();

        if (user.getEmployeeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "This account is not linked to an employee");
        }

        return user.getEmployeeId();
    }
}