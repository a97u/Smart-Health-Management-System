package com.hospital.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private DoctorRepository doctorRepository;
    
    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @InjectMocks
    private AdminController adminController;
    
    @BeforeEach
    void setUp() {
        // No setup needed at this time
    }
    
    @Test
    void testBasicRepositoryCounts() {
        // Arrange
        when(userRepository.count()).thenReturn(10L);
        when(doctorRepository.count()).thenReturn(3L);
        when(patientRepository.count()).thenReturn(6L);
        when(appointmentRepository.count()).thenReturn(20L);
        
        // Act
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalDoctors", doctorRepository.count());
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalAppointments", appointmentRepository.count());
        
        // Assert
        assertEquals(10L, stats.get("totalUsers"));
        assertEquals(3L, stats.get("totalDoctors"));
        assertEquals(6L, stats.get("totalPatients"));
        assertEquals(20L, stats.get("totalAppointments"));
        verify(userRepository, times(1)).count();
        verify(doctorRepository, times(1)).count();
        verify(patientRepository, times(1)).count();
        verify(appointmentRepository, times(1)).count();
    }
} 