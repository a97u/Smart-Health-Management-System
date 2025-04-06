package com.hospital.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.service.DoctorService;
import com.hospital.service.NurseService;
import com.hospital.service.PatientService;
import com.hospital.service.AppointmentService;
import com.hospital.service.UserService;
import com.hospital.entity.User;
import com.hospital.entity.Nurse;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for administrative functions
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private NurseService nurseService;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserService userService;

    /**
     * Get admin dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.getUserByEmail(auth.getName()).orElse(null);
            
            // Compile summary statistics
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("doctorCount", doctorService.getAllDoctors().size());
            statistics.put("nurseCount", nurseService.getAllNurses().size());
            statistics.put("patientCount", patientService.getAllPatients().size());
            statistics.put("appointmentCount", appointmentService.getAllAppointments().size());
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("user", currentUser);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error loading admin dashboard: " + e.getMessage(), e);
        }
    }

    /**
     * Get system statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // Users by role
            statistics.put("doctorCount", doctorService.getAllDoctors().size());
            statistics.put("nurseCount", nurseService.getAllNurses().size());
            statistics.put("patientCount", patientService.getAllPatients().size());
            
            // Appointments
            statistics.put("appointmentCount", appointmentService.getAllAppointments().size());
            
            // Get all appointments and count by status
            long pendingCount = appointmentService.getAllAppointments().stream()
                .filter(a -> "PENDING".equals(a.getStatus()))
                .count();
            
            long completedCount = appointmentService.getAllAppointments().stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .count();
                
            statistics.put("pendingAppointments", pendingCount);
            statistics.put("completedAppointments", completedCount);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching system statistics: " + e.getMessage(), e);
        }
    }
} 