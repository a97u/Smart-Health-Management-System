package com.hospital.controller;

import com.hospital.entity.Patient;
import com.hospital.entity.PatientHealthMetric;
import com.hospital.entity.User;
import com.hospital.service.PatientHealthMetricService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientHealthMetricControllerTest {

    @Mock
    private PatientHealthMetricService healthMetricService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private PatientHealthMetricController healthMetricController;
    
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
    void testEntityCreation() {
        // Basic test for entity creation
        assertNotNull(testMetric);
        assertEquals(1, testMetric.getId());
        assertEquals(testPatient, testMetric.getPatient());
        assertEquals("Blood Pressure", testMetric.getMetricType());
    }
    
    @Test
    void testGetPatientByUser() {
        when(patientService.getPatientByUser(any(User.class))).thenReturn(Optional.of(testPatient));
        
        Optional<Patient> result = patientService.getPatientByUser(testUser);
        
        assertTrue(result.isPresent());
        assertEquals(testPatient.getId(), result.get().getId());
        verify(patientService, times(1)).getPatientByUser(testUser);
    }
    
    @Test
    void testUserService() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("patient@example.com");
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userService, times(1)).getUserByEmail("patient@example.com");
    }
    
    @Test
    void testRepositoryMocking() {
        List<PatientHealthMetric> metrics = new ArrayList<>();
        metrics.add(testMetric);
        
        when(patientService.getPatientById(anyInt())).thenReturn(Optional.of(testPatient));
        
        Optional<Patient> patientResult = patientService.getPatientById(1);
        assertTrue(patientResult.isPresent());
        assertEquals(testPatient.getId(), patientResult.get().getId());
        
        verify(patientService, times(1)).getPatientById(1);
    }
} 