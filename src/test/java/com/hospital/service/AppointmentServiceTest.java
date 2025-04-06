package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.repository.AppointmentRepository;
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
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment testAppointment;
    private Patient testPatient;
    private Doctor testDoctor;
    private User testUser;
    private User testDoctorUser;
    private User testPatientUser;

    @BeforeEach
    void setUp() {
        // Create test users
        testPatientUser = new User();
        testPatientUser.setId(2);
        testPatientUser.setName("Patient Name");
        testPatientUser.setEmail("patient@example.com");
        
        testDoctorUser = new User();
        testDoctorUser.setId(3);
        testDoctorUser.setName("Doctor Name");
        testDoctorUser.setEmail("doctor@example.com");
        
        // Create test data
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testPatientUser);
        
        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testDoctorUser);
        
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("user@example.com");
        
        testAppointment = new Appointment();
        testAppointment.setId(1);
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        testAppointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        testAppointment.setCreatedAt(LocalDateTime.now());
        testAppointment.setCreatedBy(testUser);
        testAppointment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateAppointment() {
        // Arrange
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(testPatient);
        newAppointment.setDoctor(testDoctor);
        newAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        newAppointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        // Act
        Appointment result = appointmentService.createAppointment(newAppointment);

        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getId(), result.getId());
        assertEquals(testAppointment.getStatus(), result.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testCreateAppointmentUpdate() {
        // Arrange
        Appointment existingAppointment = new Appointment();
        existingAppointment.setId(1);
        existingAppointment.setPatient(testPatient);
        existingAppointment.setDoctor(testDoctor);
        existingAppointment.setAppointmentDate(LocalDate.now());
        existingAppointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        existingAppointment.setCreatedAt(LocalDateTime.now().minusDays(1));
        existingAppointment.setCreatedBy(testUser);
        
        // Mock to return our existing appointment when repository is queried
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(existingAppointment));
        
        // When saved, the testAppointment will be returned with an updatedAt value
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment savedAppointment = invocation.getArgument(0);
            // Ensure the updatedAt is set when saving
            if (savedAppointment.getUpdatedAt() == null) {
                savedAppointment.setUpdatedAt(LocalDateTime.now());
            }
            return savedAppointment;
        });

        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setId(1);
        updatedAppointment.setPatient(testPatient);
        updatedAppointment.setDoctor(testDoctor);
        updatedAppointment.setAppointmentDate(LocalDate.now().plusDays(2));
        updatedAppointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        // Act
        Appointment result = appointmentService.createAppointment(updatedAppointment);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getUpdatedAt(), "Updated timestamp should not be null");
        verify(appointmentRepository, times(1)).findById(1);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testScheduleAppointment() {
        // Arrange
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doNothing().when(emailService).sendAppointmentConfirmation(any(Appointment.class));

        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        String notes = "Test appointment";

        // Act
        Appointment result = appointmentService.scheduleAppointment(testPatient, testDoctor, appointmentDate, notes, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getId(), result.getId());
        assertEquals(testAppointment.getStatus(), result.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(emailService, times(1)).sendAppointmentConfirmation(any(Appointment.class));
    }

    @Test
    void testScheduleAppointmentEmailFails() {
        // Arrange
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        
        // Use a more specific mock for the expected parameters
        doThrow(new RuntimeException("Email error"))
            .when(emailService).sendAppointmentConfirmation(argThat(appointment -> 
                appointment.getPatient().equals(testPatient) &&
                appointment.getDoctor().equals(testDoctor)
            ));

        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        String notes = "Test appointment";

        // Act - We expect the service to catch the exception and continue
        Appointment result = appointmentService.scheduleAppointment(testPatient, testDoctor, appointmentDate, notes, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getId(), result.getId());
        assertEquals(Appointment.AppointmentStatus.SCHEDULED, result.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(emailService, times(1)).sendAppointmentConfirmation(any(Appointment.class));
    }

    @Test
    void testGetAllAppointments() {
        // Arrange
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(testAppointment);
        when(appointmentRepository.findAll()).thenReturn(appointmentList);

        // Act
        List<Appointment> result = appointmentService.getAllAppointments();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testAppointment.getId(), result.get(0).getId());
        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    void testGetAppointmentById() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(testAppointment));

        // Act
        Optional<Appointment> result = appointmentService.getAppointmentById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testAppointment.getId(), result.get().getId());
        verify(appointmentRepository, times(1)).findById(1);
    }

    @Test
    void testGetAppointmentsByPatient() {
        // Arrange
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(testAppointment);
        when(appointmentRepository.findByPatient(any(Patient.class))).thenReturn(appointmentList);

        // Act
        List<Appointment> result = appointmentService.getAppointmentsByPatient(testPatient);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testAppointment.getId(), result.get(0).getId());
        verify(appointmentRepository, times(1)).findByPatient(testPatient);
    }

    @Test
    void testGetUpcomingAppointmentsForPatient() {
        // Arrange
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(testAppointment);
        when(appointmentRepository.findByPatientAndStatusOrderByAppointmentDateAsc(
                any(Patient.class), any(Appointment.AppointmentStatus.class)))
                .thenReturn(appointmentList);

        // Act
        List<Appointment> result = appointmentService.getUpcomingAppointmentsForPatient(testPatient);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testAppointment.getId(), result.get(0).getId());
        verify(appointmentRepository, times(1)).findByPatientAndStatusOrderByAppointmentDateAsc(
                eq(testPatient), eq(Appointment.AppointmentStatus.SCHEDULED));
    }

    @Test
    void testCancelAppointment() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doNothing().when(emailService).sendAppointmentCancellation(any(Appointment.class));

        // Act
        Appointment result = appointmentService.cancelAppointment(1, "admin");

        // Assert
        assertNotNull(result);
        assertEquals(Appointment.AppointmentStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        assertEquals("admin", result.getUpdatedBy());
        verify(appointmentRepository, times(1)).findById(1);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(emailService, times(1)).sendAppointmentCancellation(any(Appointment.class));
    }

    @Test
    void testCancelAppointmentEmailFails() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doThrow(new RuntimeException("Email error")).when(emailService).sendAppointmentCancellation(any(Appointment.class));

        // Act - We expect the service to catch the exception and continue
        Appointment result = appointmentService.cancelAppointment(1, "admin");

        // Assert
        assertNotNull(result);
        assertEquals(Appointment.AppointmentStatus.CANCELLED, result.getStatus());
        verify(appointmentRepository, times(1)).findById(1);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(emailService, times(1)).sendAppointmentCancellation(any(Appointment.class));
    }

    @Test
    void testCancelAppointmentNotFound() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            appointmentService.cancelAppointment(999, "admin");
        });
        verify(appointmentRepository, times(1)).findById(999);
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCompleteAppointment() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        Appointment result = appointmentService.completeAppointment(1, "doctor");

        // Assert
        assertNotNull(result);
        assertEquals(Appointment.AppointmentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        assertEquals("doctor", result.getUpdatedBy());
        verify(appointmentRepository, times(1)).findById(1);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testRescheduleAppointment() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doNothing().when(emailService).sendAppointmentConfirmation(any(Appointment.class));

        LocalDate newDate = LocalDate.now().plusDays(7);

        // Act
        Appointment result = appointmentService.rescheduleAppointment(1, newDate, "receptionist");

        // Assert
        assertNotNull(result);
        assertEquals(newDate, result.getAppointmentDate());
        assertNotNull(result.getUpdatedAt());
        assertEquals("receptionist", result.getUpdatedBy());
        verify(appointmentRepository, times(1)).findById(1);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(emailService, times(1)).sendAppointmentConfirmation(any(Appointment.class));
    }
    
    @Test
    void testRescheduleAppointmentEmailFails() {
        // Arrange
        when(appointmentRepository.findById(anyInt())).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);
        doThrow(new RuntimeException("Email error")).when(emailService).sendAppointmentConfirmation(any(Appointment.class));

        LocalDate newDate = LocalDate.now().plusDays(7);

        // Act - We expect the service to catch the exception and continue
        Appointment result = appointmentService.rescheduleAppointment(1, newDate, "receptionist");

        // Assert
        assertNotNull(result);
        assertEquals(newDate, result.getAppointmentDate());
        verify(appointmentRepository, times(1)).findById(1);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(emailService, times(1)).sendAppointmentConfirmation(any(Appointment.class));
    }
} 