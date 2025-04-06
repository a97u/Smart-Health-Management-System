package com.hospital.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private NurseService nurseService;

    @Autowired
    private PatientService patientService;

    @GetMapping("/edit")
    public ResponseEntity<Map<String, Object>> showProfileEditForm(Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            
            // Set role-specific attributes
            String userRole = getUserRole(authentication);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", user);
            
            // Add role-specific data to the response
            setupProfileResponseData(responseData, user, userRole);
            
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error("Error showing profile edit form", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("name") String name,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "specialities", required = false) String specialities,
            @RequestParam(value = "yearsOfExperience", required = false) Integer yearsOfExperience,
            @RequestParam(value = "charges", required = false) BigDecimal charges,
            @RequestParam(value = "dateOfBirth", required = false) LocalDate dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            @RequestParam(value = "emergencyContact", required = false) String emergencyContact,
            Authentication authentication) {

        try {
            User user = getUserFromAuthentication(authentication);
            
            // Update common user name for all roles
            user.setName(name);
            userService.updateUser(user);

            // Update role-specific profile
            String userRole = getUserRole(authentication);
            Object updatedProfile = updateRoleSpecificProfile(
                user, phoneNumber, specialities, yearsOfExperience, charges,
                dateOfBirth, gender, address, bloodGroup, emergencyContact,
                userRole
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", user);
            response.put("profile", updatedProfile);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/edit")
    public ResponseEntity<Map<String, Object>> updateProfileRest(
            @RequestBody Map<String, Object> profileData,
            Authentication authentication) {

        try {
            User user = getUserFromAuthentication(authentication);
            
            // Update common user name for all roles
            if (profileData.containsKey("name")) {
                user.setName((String) profileData.get("name"));
                userService.updateUser(user);
            }

            // Update role-specific profile
            String userRole = getUserRole(authentication);
            String phoneNumber = (String) profileData.get("phoneNumber");
            
            // Extract role-specific fields based on user role
            Object updatedProfile = null;
            
            switch (userRole) {
                case "DOCTOR":
                    String specialities = (String) profileData.get("specialization");
                    Integer yearsOfExperience = profileData.get("yearsOfExperience") != null ? 
                        Integer.valueOf(profileData.get("yearsOfExperience").toString()) : null;
                    BigDecimal charges = profileData.get("charges") != null ?
                        new BigDecimal(profileData.get("charges").toString()) : null;
                        
                    updatedProfile = updateRoleSpecificProfile(
                        user, phoneNumber, specialities, yearsOfExperience, charges,
                        null, null, null, null, null, userRole
                    );
                    break;
                    
                case "NURSE":
                    Integer nurseYearsOfExperience = profileData.get("yearsOfExperience") != null ? 
                        Integer.valueOf(profileData.get("yearsOfExperience").toString()) : null;
                        
                    updatedProfile = updateRoleSpecificProfile(
                        user, phoneNumber, null, nurseYearsOfExperience, null,
                        null, null, null, null, null, userRole
                    );
                    break;
                    
                case "PATIENT":
                    LocalDate dateOfBirth = profileData.get("dateOfBirth") != null ?
                        LocalDate.parse((String) profileData.get("dateOfBirth")) : null;
                    String gender = (String) profileData.get("gender");
                    String address = (String) profileData.get("address");
                    String bloodGroup = (String) profileData.get("bloodGroup");
                    String emergencyContact = (String) profileData.get("emergencyContact");
                    
                    updatedProfile = updateRoleSpecificProfile(
                        user, phoneNumber, null, null, null,
                        dateOfBirth, gender, address, bloodGroup, emergencyContact, userRole
                    );
                    break;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", user);
            response.put("profile", updatedProfile);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating profile via REST API", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
 
    private User getUserFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private String getUserRole(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCTOR"))) {
            return "DOCTOR";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_NURSE"))) {
            return "NURSE";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {
            return "PATIENT";
        } else {
            throw new RuntimeException("Unsupported user role");
        }
    }
    
    private void setupProfileResponseData(Map<String, Object> responseData, User user, String userRole) {
        switch (userRole) {
            case "DOCTOR":
                Doctor doctor = doctorService.getDoctorByUser(user)
                        .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
                responseData.put("doctor", doctor);
                responseData.put("phoneNumber", doctor.getPhoneNumber());
                responseData.put("userRole", "DOCTOR");
                break;
                
            case "NURSE":
                Nurse nurse = nurseService.getNurseByUser(user)
                        .orElseThrow(() -> new RuntimeException("Nurse profile not found"));
                responseData.put("nurse", nurse);
                responseData.put("phoneNumber", nurse.getPhoneNumber());
                responseData.put("userRole", "NURSE");
                break;
                
            case "PATIENT":
                Patient patient = patientService.getPatientByUser(user)
                        .orElseThrow(() -> new RuntimeException("Patient profile not found"));
                responseData.put("patient", patient);
                responseData.put("phoneNumber", patient.getPhoneNumber());
                responseData.put("userRole", "PATIENT");
                break;
        }
    }
    
    private Object updateRoleSpecificProfile(
            User user, String phoneNumber, String specialities, 
            Integer yearsOfExperience, BigDecimal charges,
            LocalDate dateOfBirth, String gender, String address, 
            String bloodGroup, String emergencyContact, String userRole) {
            
        switch (userRole) {
            case "DOCTOR":
                Doctor doctor = doctorService.getDoctorByUser(user)
                        .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
                doctor.setPhoneNumber(phoneNumber);
                doctor.setSpecialization(specialities);
                doctor.setYearsOfExperience(yearsOfExperience);
                doctor.setCharges(charges);
                doctorService.updateDoctor(doctor);
                return doctor;
                
            case "NURSE":
                Nurse nurse = nurseService.getNurseByUser(user)
                        .orElseThrow(() -> new RuntimeException("Nurse profile not found"));
                nurse.setPhoneNumber(phoneNumber);
                nurse.setYearsOfExperience(yearsOfExperience);
                nurseService.updateNurse(nurse);
                return nurse;
                
            case "PATIENT":
                Patient patient = patientService.getPatientByUser(user)
                        .orElseThrow(() -> new RuntimeException("Patient profile not found"));
                patient.setPhoneNumber(phoneNumber);
                patient.setDateOfBirth(dateOfBirth);
                patient.setGender(gender);
                patient.setAddress(address);
                patient.setBloodGroup(bloodGroup);
                patient.setEmergencyContact(emergencyContact);
                patientService.updatePatient(patient);
                return patient;
                
            default:
                throw new RuntimeException("Unsupported user role");
        }
    }
}