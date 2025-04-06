package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private Appointment testAppointment;
    private Patient testPatient;
    private Doctor testDoctor;
    private User testPatientUser;
    private User testDoctorUser;

    @BeforeEach
    void setUp() {
        // Set up test users
        testPatientUser = new User();
        testPatientUser.setId(1);
        testPatientUser.setName("Test Patient");
        testPatientUser.setEmail("patient@example.com");

        testDoctorUser = new User();
        testDoctorUser.setId(2);
        testDoctorUser.setName("Test Doctor");
        testDoctorUser.setEmail("doctor@example.com");

        // Set up patient and doctor
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testPatientUser);

        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testDoctorUser);

        // Set up appointment
        testAppointment = new Appointment();
        testAppointment.setId(1);
        testAppointment.setPatient(testPatient);
        testAppointment.setDoctor(testDoctor);
        testAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        testAppointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
    }

    @Test
    void testSendAppointmentConfirmation() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act - test both overloaded methods
        emailService.sendAppointmentConfirmation(testAppointment);
        emailService.sendAppointmentConfirmation(
            testPatientUser.getEmail(), 
            testDoctorUser.getName(), 
            testAppointment.getAppointmentDate().toString()
        );
        
        // Assert - verify mail sender was called twice
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendAppointmentCancellation() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act
        emailService.sendAppointmentCancellation(testAppointment);
        emailService.sendAppointmentCancellationNotification(
            testPatientUser.getEmail(), 
            testDoctorUser.getName(), 
            testAppointment.getAppointmentDate().toString()
        );
        
        // Assert
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendAppointmentReminder() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act
        emailService.sendAppointmentReminder(testAppointment);
        emailService.sendAppointmentReminderNotification(
            testPatientUser.getEmail(), 
            testDoctorUser.getName(), 
            testAppointment.getAppointmentDate().toString()
        );
        
        // Assert
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendAppointmentUpdatedNotification() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act
        emailService.sendAppointmentUpdatedNotification(
            testPatientUser.getEmail(), 
            testDoctorUser.getName(), 
            testAppointment.getAppointmentDate().toString()
        );
        
        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act
        emailService.sendEmail("test@example.com", "Test Subject", "Test Body");
        
        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
    
    @Test
    void testHandleMailException() {
        // Arrange
        doThrow(new RuntimeException("Mail server connection failed")).when(mailSender).send(any(SimpleMailMessage.class));
        
        // Act & Assert - should not throw exception outside
        assertDoesNotThrow(() -> {
            emailService.sendEmail("test@example.com", "Test Subject", "Test Body");
        });
        
        // Also test other methods handle exceptions gracefully
        assertDoesNotThrow(() -> {
            emailService.sendAppointmentConfirmation(testAppointment);
        });
        
        assertDoesNotThrow(() -> {
            emailService.sendAppointmentCancellation(testAppointment);
        });
    }
} 