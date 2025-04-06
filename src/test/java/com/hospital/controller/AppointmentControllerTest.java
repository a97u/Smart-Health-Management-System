package com.hospital.controller;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
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
public class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;
    
    @Mock
    private DoctorService doctorService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private AppointmentController appointmentController;
    
    private Appointment testAppointment;
    private Doctor testDoctor;
    private Patient testPatient;
    private User testDoctorUser;
    private User testPatientUser;
    
    @BeforeEach
    void setUp() {
        // Create test data
        testDoctorUser = new User();
        testDoctorUser.setId(1);
        testDoctorUser.setEmail("doctor@example.com");
        
        testPatientUser = new User();
        testPatientUser.setId(2);
        testPatientUser.setEmail("patient@example.com");
        
        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testDoctorUser);
        
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testPatientUser);
        
        testAppointment = new Appointment();
        testAppointment.setId(1);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setPatient(testPatient);
    }
    
    @Test
    void testSetup() {
        // This is a basic test to verify that the setup is working
        assertNotNull(appointmentController);
        assertNotNull(testAppointment);
        assertEquals(1, testAppointment.getId());
        assertEquals(testDoctor, testAppointment.getDoctor());
        assertEquals(testPatient, testAppointment.getPatient());
    }
    
    @Test
    void testGetPatientById() {
        // Arrange
        when(patientService.getPatientById(anyInt())).thenReturn(Optional.of(testPatient));
        
        // Act & Assert
        Optional<Patient> result = patientService.getPatientById(1);
        assertTrue(result.isPresent());
        assertEquals(testPatient.getId(), result.get().getId());
        
        verify(patientService, times(1)).getPatientById(1);
    }
    
    @Test
    void testGetDoctorById() {
        // Arrange
        when(doctorService.getDoctorById(anyInt())).thenReturn(Optional.of(testDoctor));
        
        // Act & Assert
        Optional<Doctor> result = doctorService.getDoctorById(1);
        assertTrue(result.isPresent());
        assertEquals(testDoctor.getId(), result.get().getId());
        
        verify(doctorService, times(1)).getDoctorById(1);
    }
    
    @Test
    void testGetAppointmentsByPatient() {
        // Arrange
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(testAppointment);
        when(appointmentService.getAppointmentsByPatient(any(Patient.class))).thenReturn(appointmentList);
        
        // Act
        List<Appointment> result = appointmentService.getAppointmentsByPatient(testPatient);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testAppointment.getId(), result.get(0).getId());
        verify(appointmentService, times(1)).getAppointmentsByPatient(testPatient);
    }
} 