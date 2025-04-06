package com.hospital.controller;

import com.hospital.entity.Appointment;
import com.hospital.entity.Nurse;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.NurseService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight REST API controller for nurses
 */
@RestController
@RequestMapping("/api/nurses")
public class NurseController {

    private static final Logger logger = LoggerFactory.getLogger(NurseController.class);

    @Autowired
    private NurseService nurseService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private AppointmentService appointmentService;

    /**
     * Get nurse profile for the authenticated user
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> getNurseProfile(Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Nurse nurse = nurseService.getNurseByUser(user)
                    .orElseThrow(() -> new RuntimeException("Nurse profile not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("nurse", nurse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving nurse profile", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving nurse profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update nurse profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> updateNurseProfile(
            @RequestBody Map<String, Object> profileData,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Nurse nurse = nurseService.getNurseByUser(user)
                    .orElseThrow(() -> new RuntimeException("Nurse profile not found"));
            
            // Update user name if provided
            if (profileData.containsKey("name")) {
                user.setName((String) profileData.get("name"));
                userService.updateUser(user);
            }
            
            // Update nurse-specific fields
            if (profileData.containsKey("phoneNumber")) {
                nurse.setPhoneNumber((String) profileData.get("phoneNumber"));
            }
            
            if (profileData.containsKey("yearsOfExperience") && profileData.get("yearsOfExperience") != null) {
                nurse.setYearsOfExperience(Integer.valueOf(profileData.get("yearsOfExperience").toString()));
            }
            
            // Save updated nurse
            nurseService.updateNurse(nurse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", user);
            response.put("nurse", nurse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating nurse profile", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all patients
     */
    @GetMapping("/patients")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> getAllPatients(Authentication authentication) {
        try {
            // Get all patients
            List<Patient> patients = patientService.getAllPatients();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patients", patients);
            response.put("total", patients.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving patients", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving patients: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all appointments
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> getAllAppointments(Authentication authentication) {
        try {
            // Get all appointments
            List<Appointment> appointments = appointmentService.getAllAppointments();
            
            // Group appointments by status
            List<Appointment> scheduledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .toList();
                    
            List<Appointment> completedAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .toList();
                    
            List<Appointment> cancelledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointments", appointments);
            response.put("scheduledAppointments", scheduledAppointments);
            response.put("completedAppointments", completedAppointments);
            response.put("cancelledAppointments", cancelledAppointments);
            response.put("total", appointments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving appointments", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get patient details
     */
    @GetMapping("/patients/{id}")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable Integer id, Authentication authentication) {
        try {
            Patient patient = patientService.getPatientById(id)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            
            // Get patient's appointments
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patient", patient);
            response.put("appointments", appointments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving patient details", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving patient details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
