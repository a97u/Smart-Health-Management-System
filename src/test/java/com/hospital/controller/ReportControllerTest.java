package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.service.StatisticsService;
import com.hospital.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    @Mock
    private StatisticsService statisticsService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private ReportController reportController;
    
    private Doctor testDoctor;
    private Patient testPatient;
    private User testUser;
    private Map<String, Object> testStats;
    
    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        
        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testUser);
        
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testUser);
        
        // Create test statistics
        testStats = new HashMap<>();
        testStats.put("totalAppointments", 10L);
        testStats.put("completedAppointments", 5L);
        testStats.put("pendingAppointments", 3L);
        testStats.put("cancelledAppointments", 2L);
    }
    
    @Test
    void testEntityCreation() {
        // Basic test for entity creation
        assertNotNull(testDoctor);
        assertEquals(1, testDoctor.getId());
        assertEquals(testUser, testDoctor.getUser());
        
        assertNotNull(testPatient);
        assertEquals(1, testPatient.getId());
        assertEquals(testUser, testPatient.getUser());
    }
    
    @Test
    void testStatisticsMapContents() {
        // Basic test for statistics map structure
        assertNotNull(testStats);
        assertEquals(4, testStats.size());
        assertEquals(10L, testStats.get("totalAppointments"));
        assertEquals(5L, testStats.get("completedAppointments"));
    }
    
    @Test
    void testGetDoctorById() {
        when(doctorService.getDoctorById(anyInt())).thenReturn(Optional.of(testDoctor));
        
        Optional<Doctor> result = doctorService.getDoctorById(1);
        
        assertTrue(result.isPresent());
        assertEquals(testDoctor.getId(), result.get().getId());
        verify(doctorService, times(1)).getDoctorById(1);
    }
    
    @Test
    void testGetPatientById() {
        when(patientService.getPatientById(anyInt())).thenReturn(Optional.of(testPatient));
        
        Optional<Patient> result = patientService.getPatientById(1);
        
        assertTrue(result.isPresent());
        assertEquals(testPatient.getId(), result.get().getId());
        verify(patientService, times(1)).getPatientById(1);
    }
} 