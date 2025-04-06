package com.hospital.controller;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight REST API controller for appointments
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    /**
     * Get all appointments for the authenticated user
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllAppointments(Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Appointment> appointments;
            
            // Return appropriate appointments based on user role
            if (hasRole(authentication, "ROLE_PATIENT")) {
                Patient patient = patientService.getPatientByUser(user)
                        .orElseThrow(() -> new RuntimeException("Patient profile not found"));
                appointments = appointmentService.getAppointmentsByPatient(patient);
            } 
            else if (hasRole(authentication, "ROLE_DOCTOR")) {
                Doctor doctor = doctorService.getDoctorByUser(user)
                        .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
                appointments = appointmentService.getAppointmentsByDoctor(doctor);
            }
            else {
                appointments = appointmentService.getAllAppointments();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            response.put("success", true);
            
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
     * Book a new appointment with JSON request body
     */
    @PostMapping(value = "/book", consumes = "application/json")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestBody Map<String, Object> appointmentData,
            Authentication authentication) {
        
        try {
            Integer doctorId = Integer.valueOf(appointmentData.get("doctorId").toString());
            LocalDate appointmentDate = LocalDate.parse((String) appointmentData.get("appointmentDate"));
            String notes = (String) appointmentData.get("notes");
            
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Patient patient = patientService.getPatientByUser(user)
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            
            // Validate appointment date
            if (appointmentDate.isBefore(LocalDate.now())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Appointment date cannot be in the past");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check for appointment conflicts
            if (appointmentService.hasAppointmentConflict(doctorId, appointmentDate, null)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Doctor already has an appointment scheduled on this date");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Schedule the appointment
            Appointment savedAppointment = appointmentService.scheduleAppointment(
                    patient, doctor, appointmentDate, notes, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment booked successfully");
            response.put("appointment", savedAppointment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error booking appointment", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error booking appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * View appointment details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN', 'NURSE')")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable Integer id, Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.getAppointmentById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Security check: ensure user has permission to view this appointment
            boolean hasPermission = checkPermission(authentication, user, appointment);
            
            if (!hasPermission) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You don't have permission to view this appointment");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointment", appointment);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting appointment details", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update/reschedule appointment
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Map<String, Object>> updateAppointment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> appointmentData,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Check if user has permission to update this appointment
            boolean hasPermission = checkPermission(authentication, user, appointment);
            
            if (!hasPermission) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You don't have permission to update this appointment");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Get the appointment date from the request
            LocalDate appointmentDate = LocalDate.parse((String) appointmentData.get("appointmentDate"));
            
            // Validate appointment date
            if (appointmentDate.isBefore(LocalDate.now())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Appointment date cannot be in the past");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check for appointment conflicts
            if (appointmentService.hasAppointmentConflict(appointment.getDoctor().getId(), appointmentDate, id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Doctor already has an appointment scheduled on this date");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update appointment
            Appointment updatedAppointment = appointmentService.rescheduleAppointment(id, appointmentDate, user.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment updated successfully");
            response.put("appointment", updatedAppointment);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating appointment", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cancel an appointment
     */
    @GetMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Integer id, Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Appointment appointment = appointmentService.getAppointmentById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Check if user has permission to cancel this appointment
            boolean hasPermission = checkPermission(authentication, user, appointment);
            
            if (!hasPermission) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "You don't have permission to cancel this appointment");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Verify appointment is still scheduled
            if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Only scheduled appointments can be cancelled");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Cancel the appointment
            appointmentService.cancelAppointment(id, user.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Appointment cancelled successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cancelling appointment", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error cancelling appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Helper method to check if user has permission for an appointment
     */
    private boolean checkPermission(Authentication authentication, User user, Appointment appointment) {
        // Admin and Nurse can access any appointment
        if (hasRole(authentication, "ROLE_ADMIN") || hasRole(authentication, "ROLE_NURSE")) {
            return true;
        }
        
        // Doctors can access their own appointments
        if (hasRole(authentication, "ROLE_DOCTOR")) {
            Doctor doctor = doctorService.getDoctorByUser(user).orElse(null);
            if (doctor != null && appointment.getDoctor().getId().equals(doctor.getId())) {
                return true;
            }
        }
        
        // Patients can access their own appointments
        if (hasRole(authentication, "ROLE_PATIENT")) {
            Patient patient = patientService.getPatientByUser(user).orElse(null);
            if (patient != null && appointment.getPatient().getId().equals(patient.getId())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check whether the authenticated user has a specific role
     */
    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && 
                authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}
