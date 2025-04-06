package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.service.DoctorService;
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
public class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private DoctorController doctorController;
    
    private List<Doctor> testDoctors;
    
    @BeforeEach
    void setUp() {
        testDoctors = new ArrayList<>();
        Doctor doctor = new Doctor();
        doctor.setId(1);
        testDoctors.add(doctor);
    }
    
    @Test
    void testDoctorControllerInitialized() {
        // Simple test to verify initialization
        assertNotNull(doctorController);
        assertNotNull(doctorService);
    }
    
    @Test
    void testDoctorCreation() {
        // Basic test for entity creation
        assertNotNull(testDoctors.get(0));
        assertEquals(1, testDoctors.get(0).getId());
    }
    
    @Test
    void testFindDoctorById() {
        when(doctorService.getDoctorById(anyInt())).thenReturn(Optional.of(testDoctors.get(0)));
        
        Optional<Doctor> result = doctorService.getDoctorById(1);
        
        assertTrue(result.isPresent());
        assertEquals(testDoctors.get(0).getId(), result.get().getId());
        verify(doctorService, times(1)).getDoctorById(1);
    }
    
    @Test
    void testFindAllDoctors() {
        when(doctorService.getAllDoctors()).thenReturn(testDoctors);
        
        List<Doctor> result = doctorService.getAllDoctors();
        
        assertEquals(1, result.size());
        assertEquals(testDoctors.get(0).getId(), result.get(0).getId());
        verify(doctorService, times(1)).getAllDoctors();
    }
    
    @Test
    void testGetDoctorByUser() {
        when(doctorService.getDoctorByUser(any(User.class))).thenReturn(Optional.of(testDoctors.get(0)));
        
        Optional<Doctor> result = doctorService.getDoctorByUser(testDoctors.get(0).getUser());
        
        assertTrue(result.isPresent());
        assertEquals(testDoctors.get(0).getId(), result.get().getId());
        verify(doctorService, times(1)).getDoctorByUser(testDoctors.get(0).getUser());
    }
    
    @Test
    void testGetUserByEmail() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testDoctors.get(0).getUser()));
        
        Optional<User> result = userService.getUserByEmail(testDoctors.get(0).getUser().getEmail());
        
        assertTrue(result.isPresent());
        assertEquals(testDoctors.get(0).getUser().getId(), result.get().getId());
        verify(userService, times(1)).getUserByEmail(testDoctors.get(0).getUser().getEmail());
    }
} 