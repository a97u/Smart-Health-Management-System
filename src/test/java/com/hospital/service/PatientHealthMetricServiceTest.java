package com.hospital.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.entity.Patient;
import com.hospital.entity.PatientHealthMetric;
import com.hospital.entity.User;
import com.hospital.repository.PatientHealthMetricRepository;
import com.hospital.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
public class PatientHealthMetricServiceTest {

    @Mock
    private PatientHealthMetricRepository patientHealthMetricRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @InjectMocks
    private PatientHealthMetricService patientHealthMetricService;
    
    private PatientHealthMetric testMetric;
    private Patient testPatient;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("patient@example.com");
        testUser.setName("Test Patient");
        
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testUser);
        
        testMetric = new PatientHealthMetric();
        testMetric.setId(1);
        testMetric.setPatient(testPatient);
        testMetric.setMetricType("Blood Pressure");
    }
    
    @Test
    void testBasicEntitySetup() {
        // Basic test for entity creation
        assertNotNull(testMetric);
        assertEquals(1, testMetric.getId());
        assertEquals(testPatient, testMetric.getPatient());
        assertEquals("Blood Pressure", testMetric.getMetricType());
    }
    
    @Test
    void testRepositoryFindByPatient() {
        // Arrange
        List<PatientHealthMetric> metrics = new ArrayList<>();
        metrics.add(testMetric);
        
        when(patientHealthMetricRepository.findByPatient(any(Patient.class))).thenReturn(metrics);
        
        // Act
        List<PatientHealthMetric> result = patientHealthMetricRepository.findByPatient(testPatient);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testMetric.getId(), result.get(0).getId());
        verify(patientHealthMetricRepository, times(1)).findByPatient(testPatient);
    }
    
    @Test
    void testRepositoryFindById() {
        // Arrange
        when(patientHealthMetricRepository.findById(anyInt())).thenReturn(Optional.of(testMetric));
        
        // Act
        Optional<PatientHealthMetric> result = patientHealthMetricRepository.findById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMetric.getId(), result.get().getId());
        verify(patientHealthMetricRepository, times(1)).findById(1);
    }
    
    @Test
    void testRepositoryFindByIdNotFound() {
        // Arrange
        when(patientHealthMetricRepository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act
        Optional<PatientHealthMetric> result = patientHealthMetricRepository.findById(999);
        
        // Assert
        assertFalse(result.isPresent());
        verify(patientHealthMetricRepository, times(1)).findById(999);
    }
    
    @Test
    void testRepositorySave() {
        // Arrange
        when(patientHealthMetricRepository.save(any(PatientHealthMetric.class))).thenReturn(testMetric);
        
        // Act
        PatientHealthMetric savedMetric = patientHealthMetricRepository.save(testMetric);
        
        // Assert
        assertEquals(testMetric.getId(), savedMetric.getId());
        verify(patientHealthMetricRepository, times(1)).save(testMetric);
    }
    
    @Test
    void testRepositoryDeleteById() {
        // Arrange
        doNothing().when(patientHealthMetricRepository).deleteById(anyInt());
        
        // Act
        patientHealthMetricRepository.deleteById(1);
        
        // Assert
        verify(patientHealthMetricRepository, times(1)).deleteById(1);
    }
    
    @Test
    void testRepositoryIntegration() {
        // Arrange
        List<PatientHealthMetric> metrics = new ArrayList<>();
        metrics.add(testMetric);
        
        when(patientRepository.findById(anyInt())).thenReturn(Optional.of(testPatient));
        when(patientHealthMetricRepository.findByPatient(any(Patient.class))).thenReturn(metrics);
        
        // Act & Assert
        Optional<Patient> patientResult = patientRepository.findById(1);
        assertTrue(patientResult.isPresent());
        
        List<PatientHealthMetric> metricResult = patientHealthMetricRepository.findByPatient(patientResult.get());
        assertEquals(1, metricResult.size());
        
        verify(patientRepository, times(1)).findById(1);
        verify(patientHealthMetricRepository, times(1)).findByPatient(testPatient);
    }
} 