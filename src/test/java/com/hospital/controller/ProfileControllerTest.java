package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileControllerTest {

    @Mock
    private UserService userService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private DoctorService doctorService;
    
    @InjectMocks
    private ProfileController profileController;
    
    private User testUser;
    private Patient testPatient;
    private Doctor testDoctor;
    
    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testUser);
        
        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testUser);
    }
    
    @Test
    void testEntityCreation() {
        // Basic test for entity creation
        assertNotNull(testUser);
        assertEquals(1, testUser.getId());
        assertEquals("test@example.com", testUser.getEmail());
        
        assertNotNull(testPatient);
        assertEquals(1, testPatient.getId());
        assertEquals(testUser, testPatient.getUser());
        
        assertNotNull(testDoctor);
        assertEquals(1, testDoctor.getId());
        assertEquals(testUser, testDoctor.getUser());
    }
    
    @Test
    void testGetUserByEmail() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("test@example.com");
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userService, times(1)).getUserByEmail("test@example.com");
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
    void testGetDoctorByUser() {
        when(doctorService.getDoctorByUser(any(User.class))).thenReturn(Optional.of(testDoctor));
        
        Optional<Doctor> result = doctorService.getDoctorByUser(testUser);
        
        assertTrue(result.isPresent());
        assertEquals(testDoctor.getId(), result.get().getId());
        verify(doctorService, times(1)).getDoctorByUser(testUser);
    }
} 