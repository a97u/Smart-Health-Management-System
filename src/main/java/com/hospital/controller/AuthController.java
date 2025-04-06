package com.hospital.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.entity.Doctor;
import com.hospital.entity.Nurse;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.DoctorService;
import com.hospital.service.NurseService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Controller responsible for all authentication-related operations including
 * login, registration, and dashboard redirection.
 */
@RestController
@RequestMapping("/api")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private NurseService nurseService;
    
    // =============== LOGIN RELATED ENDPOINTS ===============
    
    // Removed /login endpoint as requested
    
    /**
     * Dashboard redirection based on user role
     */
    // Removed / endpoint as requested
    
    /**
     * Explicit mapping for /home URL
     * @return Home page information
     */
    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> homePage() {
        logger.debug("Home URL accessed, serving home page");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Healthcare API Home");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        logger.debug("Dashboard controller called, providing role information");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Get user roles
        Set<String> roles = auth.getAuthorities().stream()
            .map(authority -> authority.getAuthority().replace("ROLE_", ""))
            .collect(java.util.stream.Collectors.toSet());
        
        logger.debug("User roles: {}", roles);
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("roles", roles);
        
        // Determine dashboard URL based on role
        String dashboardUrl = "/api";
        if (roles.contains("ADMIN")) {
            dashboardUrl = "/api/admin/dashboard";
        } else if (roles.contains("DOCTOR")) {
            dashboardUrl = "/api/doctor/dashboard";
        } else if (roles.contains("NURSE")) {
            dashboardUrl = "/api/nurse/dashboard";
        } else if (roles.contains("PATIENT")) {
            dashboardUrl = "/api/patient/dashboard";
        }
        
        response.put("dashboardUrl", dashboardUrl);
        return ResponseEntity.ok(response);
    }
    
    // =============== REGISTRATION RELATED ENDPOINTS ===============
    
    // GET registration endpoints removed as requested
    
    /**
     * Process doctor registration
     */
    @PostMapping("/auth/register/doctor")
    public ResponseEntity<Map<String, Object>> processDoctorRegistration(
            @RequestBody Map<String, Object> registrationData) {
        try {
            Map<String, Object> userMap = (Map<String, Object>) registrationData.get("user");
            Map<String, Object> doctorMap = (Map<String, Object>) registrationData.get("doctor");
            String confirmPassword = (String) registrationData.get("confirmPassword");
            
            User user = new User();
            user.setEmail((String) userMap.get("email"));
            user.setPassword((String) userMap.get("password"));
            user.setName((String) userMap.get("name"));
            
            Doctor doctor = new Doctor();
            doctor.setSpecialization((String) doctorMap.get("specialization"));
            
            logger.debug("Processing doctor registration for email: {}", user.getEmail());
            
            // First validate that passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Register the doctor
            doctorService.registerDoctor(doctor, user);
            logger.debug("Doctor registered successfully: {}", user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Please login with your credentials.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error during doctor registration", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Process nurse registration
     */
    @PostMapping("/auth/register/nurse")
    public ResponseEntity<Map<String, Object>> processNurseRegistration(
            @RequestBody Map<String, Object> registrationData) {
        try {
            Map<String, Object> userMap = (Map<String, Object>) registrationData.get("user");
            String confirmPassword = (String) registrationData.get("confirmPassword");
            
            User user = new User();
            user.setEmail((String) userMap.get("email"));
            user.setPassword((String) userMap.get("password"));
            user.setName((String) userMap.get("name"));
            
            Nurse nurse = new Nurse();
            
            logger.debug("Processing nurse registration for email: {}", user.getEmail());
            
            // First validate that passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Register the nurse
            nurseService.registerNurse(nurse, user);
            logger.debug("Nurse registered successfully: {}", user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Please login with your credentials.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error during nurse registration", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Process patient registration
     */
    @PostMapping("/auth/register/patient")
    public ResponseEntity<Map<String, Object>> processPatientRegistration(
            @RequestBody Map<String, Object> registrationData) {
        try {
            Map<String, Object> userMap = (Map<String, Object>) registrationData.get("user");
            Map<String, Object> patientMap = (Map<String, Object>) registrationData.get("patient");
            String confirmPassword = (String) registrationData.get("confirmPassword");
            
            User user = new User();
            user.setEmail((String) userMap.get("email"));
            user.setPassword((String) userMap.get("password"));
            user.setName((String) userMap.get("name"));
            
            Patient patient = new Patient();
            
            // Handle date conversion if provided
            if (patientMap.get("dateOfBirth") != null) {
                patient.setDateOfBirth(LocalDate.parse((String) patientMap.get("dateOfBirth")));
            }
            
            patient.setGender((String) patientMap.get("gender"));
            patient.setAddress((String) patientMap.get("address"));
            patient.setPhoneNumber((String) patientMap.get("phoneNumber"));
            
            logger.debug("Processing patient registration for email: {}", user.getEmail());
            
            // First validate that passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Register the patient
            patientService.registerPatient(patient, user);
            logger.debug("Patient registered successfully: {}", user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Please login with your credentials.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error during patient registration", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}