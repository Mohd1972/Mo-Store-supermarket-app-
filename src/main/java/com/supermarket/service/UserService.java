package com.supermarket.service;

import com.supermarket.entity.Role;
import com.supermarket.entity.User;
import com.supermarket.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public void save(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            User existing = userRepository.findByUsername(user.getUsername())
                    .filter(u -> user.getId() == null || !u.getId().equals(user.getId()))
                    .orElse(null);
            if (existing != null) {
                throw new IllegalArgumentException("Username already exists");
            }
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()
                && !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public boolean hasAdmin() {
        return userRepository.findAll().stream().anyMatch(u -> u.getRole() == Role.ADMIN);
    }
}
