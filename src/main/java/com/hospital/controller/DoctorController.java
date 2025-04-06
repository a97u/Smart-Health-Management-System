package com.hospital.controller;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight REST API controller for doctors
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    
    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private MedicalRecordService medicalRecordService;
    
    @Autowired
    private PatientService patientService;

    /**
     * Get doctor profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getDoctorProfile(Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
            Doctor doctor = doctorService.getDoctorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("doctor", doctor);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving doctor profile", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving doctor profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update doctor profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> updateDoctorProfile(
            @RequestBody Map<String, Object> profileData,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Doctor doctor = doctorService.getDoctorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            
            // Update user name if provided
            if (profileData.containsKey("name")) {
                user.setName((String) profileData.get("name"));
                userService.updateUser(user);
            }
            
            // Update doctor-specific fields
            if (profileData.containsKey("phoneNumber")) {
                doctor.setPhoneNumber((String) profileData.get("phoneNumber"));
            }
            
            if (profileData.containsKey("specialization")) {
                doctor.setSpecialization((String) profileData.get("specialization"));
            }
            
            if (profileData.containsKey("yearsOfExperience") && profileData.get("yearsOfExperience") != null) {
                doctor.setYearsOfExperience(Integer.valueOf(profileData.get("yearsOfExperience").toString()));
            }
            
            if (profileData.containsKey("charges") && profileData.get("charges") != null) {
                doctor.setCharges(new java.math.BigDecimal(profileData.get("charges").toString()));
            }
            
            // Save updated doctor
            doctorService.updateDoctor(doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("user", user);
            response.put("doctor", doctor);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating doctor profile", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get doctor's patients
     */
    @GetMapping("/patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getDoctorPatients(Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Doctor doctor = doctorService.getDoctorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            
            // Get all appointments for this doctor
            List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor);
            
            // Extract unique patients from appointments
            List<Patient> patients = appointments.stream()
                    .map(Appointment::getPatient)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patients", patients);
            response.put("total", patients.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving doctor's patients", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving patients: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get patient details
     */
    @GetMapping("/patients/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getPatientDetails(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Doctor doctor = doctorService.getDoctorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            
            Patient patient = patientService.getPatientById(id)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            
            // Get patient's appointments with this doctor
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient).stream()
                    .filter(a -> a.getDoctor().getId().equals(doctor.getId()))
                    .collect(Collectors.toList());
            
            // Get patient's medical records created by this doctor
            List<MedicalRecord> medicalRecords = medicalRecordService.getMedicalRecordsByPatient(patient).stream()
                    .filter(r -> r.getDoctor().getId().equals(doctor.getId()))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("patient", patient);
            response.put("appointments", appointments);
            response.put("medicalRecords", medicalRecords);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving patient details", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving patient details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get doctor's appointments
     */
    @GetMapping("/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getDoctorAppointments(Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Doctor doctor = doctorService.getDoctorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            
            // Get all appointments for this doctor
            List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctor);
            
            // Group appointments by status
            List<Appointment> scheduledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .collect(Collectors.toList());
                    
            List<Appointment> completedAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                    .collect(Collectors.toList());
                    
            List<Appointment> cancelledAppointments = appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                    .collect(Collectors.toList());
            
            // Get today's appointments
            LocalDate today = LocalDate.now();
            List<Appointment> todayAppointments = appointments.stream()
                    .filter(a -> a.getAppointmentDate().equals(today))
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("appointments", appointments);
            response.put("scheduledAppointments", scheduledAppointments);
            response.put("completedAppointments", completedAppointments);
            response.put("cancelledAppointments", cancelledAppointments);
            response.put("todayAppointments", todayAppointments);
            response.put("total", appointments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving doctor's appointments", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving appointments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Create a medical record
     */
    @PostMapping("/records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> createMedicalRecord(
            @RequestBody Map<String, Object> recordData,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Doctor doctor = doctorService.getDoctorByUser(user)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
            
            Integer patientId = Integer.valueOf(recordData.get("patientId").toString());
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            
            LocalDate visitDate = LocalDate.parse((String) recordData.get("visitDate"));
            String symptoms = (String) recordData.get("symptoms");
            String diagnosis = (String) recordData.get("diagnosis");
            String treatment = (String) recordData.get("treatment");
            String notes = (String) recordData.get("notes");
            
            // Create and save the medical record
            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setPatient(patient);
            medicalRecord.setDoctor(doctor);
            medicalRecord.setVisitDate(visitDate);
            medicalRecord.setDiagnosis(diagnosis);
            medicalRecord.setPrescription(treatment);
            
            MedicalRecord savedRecord = medicalRecordService.createMedicalRecord(medicalRecord);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Medical record created successfully");
            response.put("medicalRecord", savedRecord);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating medical record", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating medical record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
