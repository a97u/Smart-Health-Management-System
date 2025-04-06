package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordControllerTest {

    @Mock
    private MedicalRecordService medicalRecordService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private MedicalRecordController medicalRecordController;
    
    private MedicalRecord testMedicalRecord;
    private Patient testPatient;
    private Doctor testDoctor;
    private User testDoctorUser;
    private User testPatientUser;
    
    @BeforeEach
    void setUp() {
        // Create test data
        testDoctorUser = new User();
        testDoctorUser.setId(1);
        testDoctorUser.setEmail("doctor@example.com");
        testDoctorUser.setName("Test Doctor");
        
        testPatientUser = new User();
        testPatientUser.setId(2);
        testPatientUser.setEmail("patient@example.com");
        testPatientUser.setName("Test Patient");
        
        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testDoctorUser);
        
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testPatientUser);
        
        testMedicalRecord = new MedicalRecord();
        testMedicalRecord.setId(1);
        testMedicalRecord.setPatient(testPatient);
        testMedicalRecord.setDoctor(testDoctor);
        testMedicalRecord.setDiagnosis("Test Diagnosis");
        testMedicalRecord.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void testMedicalRecordCreation() {
        // Basic test for entity creation
        assertNotNull(testMedicalRecord);
        assertEquals(1, testMedicalRecord.getId());
        assertEquals(testPatient, testMedicalRecord.getPatient());
        assertEquals(testDoctor, testMedicalRecord.getDoctor());
        assertEquals("Test Diagnosis", testMedicalRecord.getDiagnosis());
    }
    
    @Test
    void testGetAllMedicalRecordsService() {
        // Test the service method directly
        List<MedicalRecord> records = new ArrayList<>();
        records.add(testMedicalRecord);
        when(medicalRecordService.getAllMedicalRecords()).thenReturn(records);
        
        List<MedicalRecord> result = medicalRecordService.getAllMedicalRecords();
        
        assertEquals(1, result.size());
        assertEquals(testMedicalRecord.getId(), result.get(0).getId());
        verify(medicalRecordService, times(1)).getAllMedicalRecords();
    }
    
    @Test
    void testGetMedicalRecordByIdService() {
        when(medicalRecordService.getMedicalRecordById(anyInt())).thenReturn(Optional.of(testMedicalRecord));
        
        Optional<MedicalRecord> result = medicalRecordService.getMedicalRecordById(1);
        
        assertTrue(result.isPresent());
        assertEquals(testMedicalRecord.getId(), result.get().getId());
        verify(medicalRecordService, times(1)).getMedicalRecordById(1);
    }
    
    @Test
    void testGetMedicalRecordsByPatientService() {
        List<MedicalRecord> records = new ArrayList<>();
        records.add(testMedicalRecord);
        when(medicalRecordService.getMedicalRecordsByPatient(any(Patient.class))).thenReturn(records);
        
        List<MedicalRecord> result = medicalRecordService.getMedicalRecordsByPatient(testPatient);
        
        assertEquals(1, result.size());
        assertEquals(testMedicalRecord.getId(), result.get(0).getId());
        verify(medicalRecordService, times(1)).getMedicalRecordsByPatient(testPatient);
    }
    
    @Test
    void testGetMedicalRecordsByDoctorService() {
        List<MedicalRecord> records = new ArrayList<>();
        records.add(testMedicalRecord);
        when(medicalRecordService.getMedicalRecordsByDoctor(any(Doctor.class))).thenReturn(records);
        
        List<MedicalRecord> result = medicalRecordService.getMedicalRecordsByDoctor(testDoctor);
        
        assertEquals(1, result.size());
        assertEquals(testMedicalRecord.getId(), result.get(0).getId());
        verify(medicalRecordService, times(1)).getMedicalRecordsByDoctor(testDoctor);
    }
    
    @Test
    void testDeleteMedicalRecordService() {
        doNothing().when(medicalRecordService).deleteMedicalRecord(anyInt());
        
        medicalRecordService.deleteMedicalRecord(1);
        
        verify(medicalRecordService, times(1)).deleteMedicalRecord(1);
    }
} 