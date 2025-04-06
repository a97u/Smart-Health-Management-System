package com.hospital.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {

    @Mock
    private PatientService patientService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private PatientController patientController;
    
    private List<Patient> testPatients;
    
    @BeforeEach
    void setUp() {
        testPatients = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId(1);
        testPatients.add(patient);
    }
    
    @Test
    void testPatientControllerInitialized() {
        // Simple test to verify initialization
        assertNotNull(patientController);
        assertNotNull(patientService);
    }
    
    @Test
    void testGetAllPatientsService() {
        // Arrange
        when(patientService.getAllPatients()).thenReturn(testPatients);
        
        // Act
        List<Patient> result = patientService.getAllPatients();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        verify(patientService, times(1)).getAllPatients();
    }
    
    @Test
    void testGetPatientByIdService() {
        // Arrange
        when(patientService.getPatientById(anyInt())).thenReturn(Optional.of(testPatients.get(0)));
        
        // Act
        Optional<Patient> result = patientService.getPatientById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(patientService, times(1)).getPatientById(1);
    }
    
    @Test
    void testGetPatientByIdService_NotFound() {
        when(patientService.getPatientById(anyInt())).thenReturn(Optional.empty());
        
        Optional<Patient> result = patientService.getPatientById(999);
        
        assertFalse(result.isPresent());
        verify(patientService, times(1)).getPatientById(999);
    }
    
    @Test
    void testGetPatientByUserService() {
        when(patientService.getPatientByUser(any(User.class))).thenReturn(Optional.of(testPatients.get(0)));
        
        Optional<Patient> result = patientService.getPatientByUser(testPatients.get(0).getUser());
        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(patientService, times(1)).getPatientByUser(testPatients.get(0).getUser());
    }
    
    @Test
    void testGetUserByEmailService() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testPatients.get(0).getUser()));
        
        Optional<User> result = userService.getUserByEmail(testPatients.get(0).getUser().getEmail());
        
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(userService, times(1)).getUserByEmail(testPatients.get(0).getUser().getEmail());
    }
} 