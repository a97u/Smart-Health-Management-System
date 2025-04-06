package com.hospital.controller;

import com.hospital.service.StatisticsService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.service.AppointmentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for reports and statistics
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private AppointmentService appointmentService;

    /**
     * Get system overview statistics
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Add core statistics
            response.put("statistics", statisticsService.getAdminDashboardStatistics());
            response.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating system overview report", e);
            throw new RuntimeException("Failed to generate system overview: " + e.getMessage());
        }
    }

    /**
     * Get detailed statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDetailedStatistics() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Administrative statistics
            response.put("adminStats", statisticsService.getAdminDashboardStatistics());
            
            // User statistics
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("doctorCount", doctorService.getAllDoctors().size());
            userStats.put("patientCount", patientService.getAllPatients().size());
            response.put("userStats", userStats);
            
            // Appointment statistics
            Map<String, Object> appointmentStats = new HashMap<>();
            appointmentStats.put("total", appointmentService.getAllAppointments().size());
            response.put("appointmentStats", appointmentStats);
            
            // Include timestamp
            response.put("reportDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating detailed statistics", e);
            throw new RuntimeException("Failed to generate detailed statistics: " + e.getMessage());
        }
    }
    
    // Doctor performance endpoint removed as requested
} 