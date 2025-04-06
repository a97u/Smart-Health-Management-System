package com.hospital.controller;

import com.hospital.entity.Appointment;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.PatientHealthMetric;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientHealthMetricService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API controller for patient management
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PatientHealthMetricService healthMetricService;

    /**
     * Get authenticated patient
     */
    private Patient getAuthenticatedPatient(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return patientService.getPatientByUser(user)
                .orElseThrow(() -> new RuntimeException("Patient not found for authenticated user"));
    }

    /**
     * Get all patients (for admin, doctors, nurses)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Get patient by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #id)")
    public ResponseEntity<Patient> getPatientById(@PathVariable Integer id) {
        Patient patient = patientService.getPatientById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + id));
        return ResponseEntity.ok(patient);
    }

    /**
     * Update patient information
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #id)")
    public ResponseEntity<Patient> updatePatient(@PathVariable Integer id, @RequestBody Patient updatedPatient) {
        try {
            Patient patient = patientService.getPatientById(id)
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + id));
            
            // Copy relevant fields from the request
            // Assumes all necessary fields are directly set in the Patient object
            
            Patient savedPatient = patientService.updatePatient(patient);
            return ResponseEntity.ok(savedPatient);
        } catch (Exception e) {
            logger.error("Error updating patient", e);
            throw new RuntimeException("Failed to update patient: " + e.getMessage());
        }
    }

    /**
     * Delete a patient
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Integer id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting patient", e);
            throw new RuntimeException("Failed to delete patient: " + e.getMessage());
        }
    }

    /**
     * Get patient dashboard data
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard(Authentication authentication) {
        try {
            Patient patient = getAuthenticatedPatient(authentication);
            
            // Get upcoming appointments
            List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointmentsByPatient(patient);
            
            // Get recent medical records
            List<MedicalRecord> recentRecords = medicalRecordService.getRecentMedicalRecordsByPatient(patient, 5);
            
            // Get latest health metrics
            List<PatientHealthMetric> recentMetrics = healthMetricService.getRecentHealthMetricsByPatient(patient, 5);
            
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("patient", patient);
            dashboardData.put("upcomingAppointments", upcomingAppointments);
            dashboardData.put("recentRecords", recentRecords);
            dashboardData.put("recentMetrics", recentMetrics);
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("Error loading patient dashboard", e);
            throw new RuntimeException("Failed to load dashboard: " + e.getMessage());
        }
    }

    /**
     * Get patient appointments
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAppointments(Authentication authentication) {
        try {
            Patient patient = getAuthenticatedPatient(authentication);
            
            // Get upcoming appointments
            List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointmentsByPatient(patient);
            
            // Get past appointments
            List<Appointment> pastAppointments = appointmentService.getPastAppointmentsByPatient(patient);
            
            Map<String, Object> response = new HashMap<>();
            response.put("upcomingAppointments", upcomingAppointments);
            response.put("pastAppointments", pastAppointments);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error loading patient appointments", e);
            throw new RuntimeException("Failed to load appointments: " + e.getMessage());
        }
    }

    /**
     * Get patient health metrics
     */
    @GetMapping("/health-metrics")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getHealthMetrics(Authentication authentication) {
        try {
            Patient patient = getAuthenticatedPatient(authentication);
            
            // Get health metrics for charts
            Map<String, Object> chartData = healthMetricService.getPatientHealthMetricsChartData(patient);
            
            // Get recent health metrics entries
            List<PatientHealthMetric> recentMetrics = healthMetricService.getRecentHealthMetricsByPatient(patient, 10);
            
            Map<String, Object> response = new HashMap<>();
            response.put("chartData", chartData);
            response.put("recentMetrics", recentMetrics);
            response.put("chartUrls", Map.of(
                "weight", "/api/health-metrics/chart/" + patient.getId() + "/weight",
                "bloodPressure", "/api/health-metrics/chart/" + patient.getId() + "/blood-pressure",
                "heartRate", "/api/health-metrics/chart/" + patient.getId() + "/heart-rate"
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error viewing health metrics", e);
            throw new RuntimeException("Failed to load health metrics: " + e.getMessage());
        }
    }
    
    /**
     * Get patient medical records
     */
    @GetMapping("/medical-records")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecords(Authentication authentication) {
        try {
            Patient patient = getAuthenticatedPatient(authentication);
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByPatient(patient);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error getting medical records", e);
            throw new RuntimeException("Failed to get medical records: " + e.getMessage());
        }
    }
}