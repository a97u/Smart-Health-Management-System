package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private MedicalRecord testRecord;
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

        testRecord = new MedicalRecord();
        testRecord.setId(1);
        testRecord.setPatient(testPatient);
        testRecord.setDoctor(testDoctor);
        testRecord.setDiagnosis("Test Diagnosis");
        testRecord.setPrescription("Test Prescription");
        testRecord.setVisitDate(LocalDate.now());
        testRecord.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllMedicalRecords() {
        // Arrange
        List<MedicalRecord> recordList = new ArrayList<>();
        recordList.add(testRecord);
        when(medicalRecordRepository.findAll()).thenReturn(recordList);

        // Act
        List<MedicalRecord> result = medicalRecordService.getAllMedicalRecords();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRecord.getId(), result.get(0).getId());
        verify(medicalRecordRepository, times(1)).findAll();
    }

    @Test
    void testGetMedicalRecordById() {
        // Arrange
        when(medicalRecordRepository.findById(anyInt())).thenReturn(Optional.of(testRecord));

        // Act
        Optional<MedicalRecord> result = medicalRecordService.getMedicalRecordById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRecord.getId(), result.get().getId());
        verify(medicalRecordRepository, times(1)).findById(1);
    }

    @Test
    void testGetMedicalRecordsByPatient() {
        // Arrange
        List<MedicalRecord> recordList = new ArrayList<>();
        recordList.add(testRecord);
        when(medicalRecordRepository.findByPatient(any(Patient.class))).thenReturn(recordList);

        // Act
        List<MedicalRecord> result = medicalRecordService.getMedicalRecordsByPatient(testPatient);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRecord.getId(), result.get(0).getId());
        verify(medicalRecordRepository, times(1)).findByPatient(testPatient);
    }

    @Test
    void testGetMedicalRecordsByDoctor() {
        // Arrange
        List<MedicalRecord> recordList = new ArrayList<>();
        recordList.add(testRecord);
        when(medicalRecordRepository.findByDoctor(any(Doctor.class))).thenReturn(recordList);

        // Act
        List<MedicalRecord> result = medicalRecordService.getMedicalRecordsByDoctor(testDoctor);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRecord.getId(), result.get(0).getId());
        verify(medicalRecordRepository, times(1)).findByDoctor(testDoctor);
    }

    @Test
    void testCreateMedicalRecord() {
        // Arrange
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testRecord);

        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setPatient(testPatient);
        newRecord.setDoctor(testDoctor);
        newRecord.setDiagnosis("New Diagnosis");
        newRecord.setPrescription("New Prescription");
        newRecord.setVisitDate(LocalDate.now());

        // Act
        MedicalRecord result = medicalRecordService.createMedicalRecord(newRecord);

        // Assert
        assertNotNull(result);
        assertEquals(testRecord.getId(), result.getId());
        verify(medicalRecordRepository, times(1)).save(newRecord);
    }

    @Test
    void testUpdateMedicalRecord() {
        // Arrange
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(testRecord);

        // Act
        MedicalRecord result = medicalRecordService.updateMedicalRecord(testRecord);

        // Assert
        assertNotNull(result);
        assertEquals(testRecord.getId(), result.getId());
        verify(medicalRecordRepository, times(1)).save(testRecord);
    }

    @Test
    void testDeleteMedicalRecord() {
        // Arrange
        doNothing().when(medicalRecordRepository).deleteById(anyInt());

        // Act
        medicalRecordService.deleteMedicalRecord(1);

        // Assert
        verify(medicalRecordRepository, times(1)).deleteById(1);
    }

    @Test
    void testGetRecentMedicalRecordsByPatient() {
        // Arrange
        List<MedicalRecord> recordList = new ArrayList<>();
        recordList.add(testRecord);
        when(medicalRecordRepository.findByPatientOrderByVisitDateDesc(any(Patient.class))).thenReturn(recordList);

        // Act
        List<MedicalRecord> result = medicalRecordService.getRecentMedicalRecordsByPatient(testPatient, 5);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testRecord.getId(), result.get(0).getId());
        verify(medicalRecordRepository, times(1)).findByPatientOrderByVisitDateDesc(testPatient);
    }
} 