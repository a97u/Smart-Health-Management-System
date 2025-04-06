package com.hospital.service;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.UserRepository;
import com.hospital.repository.NurseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NurseRepository nurseRepository;

    @Transactional
    public User registerUser(User user, Role.RoleName roleName) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found."));
        user.setRoles(Collections.singleton(role));

        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Check if a user with the given email already exists
     * @param email The email to check
     * @return True if a user with this email exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User updateUser(User user) {
        // Check if user exists first
        getUserById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));
            
        // If updating password, make sure it's encoded
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Ensure roles are properly set
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            // Get existing user's roles to preserve them
            User existingUser = getUserById(user.getId()).get();
            user.setRoles(existingUser.getRoles());
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        // Check if user exists first
        User user = getUserById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            
        // Check if user has associated entities that would cause a foreign key violation
        // For simplicity, we're just checking if user exists. In a real application, 
        // you would need to check all related entities (Doctor, Patient, Nurse, etc.)
        
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Could not delete user. The user may have associated records: " + e.getMessage());
        }
    }
    
    /**
     * Finds users with NURSE role but no Nurse profile
     * @return List of users that need a nurse profile to be created
     */
    @Transactional(readOnly = true)
    public List<User> findUsersWithNurseRoleButNoProfile() {
        Role nurseRole = roleRepository.findByName(Role.RoleName.NURSE)
                .orElseThrow(() -> new RuntimeException("Nurse role not found"));
        
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(nurseRole))
                .filter(user -> nurseRepository.findByUser(user).isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets recent user registrations
     * @return List of recently registered users
     */
    @Transactional(readOnly = true)
    public List<User> getRecentRegistrations() {
        // Get registrations from the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(thirtyDaysAgo))
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt())) // Sort newest first
                .limit(10) // Limit to 10 most recent
                .collect(Collectors.toList());
    }
}