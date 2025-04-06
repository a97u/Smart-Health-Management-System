package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * REST API controller for medical records
 */
@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    /**
     * Get all medical records
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        try {
            List<MedicalRecord> records = medicalRecordService.getAllMedicalRecords();
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error retrieving all medical records", e);
            throw new RuntimeException("Failed to retrieve medical records: " + e.getMessage());
        }
    }

    /**
     * Get medical record by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @recordSecurity.canViewRecord(authentication, #id)")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(@PathVariable Integer id) {
        try {
            MedicalRecord record = medicalRecordService.getMedicalRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + id));
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            logger.error("Error retrieving medical record with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve medical record: " + e.getMessage());
        }
    }

    /**
     * Get medical records by patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsByPatient(@PathVariable Integer patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            List<MedicalRecord> records = medicalRecordService.getMedicalRecordsByPatient(patient);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error retrieving medical records for patient ID: {}", patientId, e);
            throw new RuntimeException("Failed to retrieve patient medical records: " + e.getMessage());
        }
    }

    /**
     * Create a new medical record
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public ResponseEntity<MedicalRecord> createMedicalRecord(@RequestBody Map<String, Object> recordData) {
        try {
            Integer patientId = (Integer) recordData.get("patientId");
            Integer doctorId = (Integer) recordData.get("doctorId");
            String diagnosis = (String) recordData.get("diagnosis");
            String prescription = (String) recordData.get("prescription");
            LocalDate visitDate = recordData.get("visitDate") != null ? 
                LocalDate.parse((String) recordData.get("visitDate")) : 
                LocalDate.now();
            
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            Doctor doctor = doctorService.getDoctorById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
            
            MedicalRecord record = medicalRecordService.createMedicalRecord(patient, doctor, visitDate, prescription);
            
            if (diagnosis != null && !diagnosis.isEmpty()) {
                record.setDiagnosis(diagnosis);
                record = medicalRecordService.updateMedicalRecord(record);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(record);
        } catch (Exception e) {
            logger.error("Error creating medical record", e);
            throw new RuntimeException("Failed to create medical record: " + e.getMessage());
        }
    }

    /**
     * Update a medical record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public ResponseEntity<MedicalRecord> updateMedicalRecord(
            @PathVariable Integer id, 
            @RequestBody Map<String, Object> updateData) {
        try {
            MedicalRecord record = medicalRecordService.getMedicalRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + id));
            
            if (updateData.containsKey("diagnosis")) {
                record.setDiagnosis((String) updateData.get("diagnosis"));
            }
            
            if (updateData.containsKey("prescription")) {
                record.setPrescription((String) updateData.get("prescription"));
            }
            
            if (updateData.containsKey("status")) {
                String statusStr = (String) updateData.get("status");
                record.setStatus(MedicalRecord.RecordStatus.valueOf(statusStr));
            }
            
            if (updateData.containsKey("visitDate")) {
                record.setVisitDate(LocalDate.parse((String) updateData.get("visitDate")));
            }
            
            MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(record);
            return ResponseEntity.ok(updatedRecord);
        } catch (Exception e) {
            logger.error("Error updating medical record with ID: {}", id, e);
            throw new RuntimeException("Failed to update medical record: " + e.getMessage());
        }
    }
}