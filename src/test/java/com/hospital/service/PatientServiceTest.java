package com.hospital.service;

import com.hospital.entity.Patient;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private PatientService patientService;
    
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
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testPatient.setGender("Male");
        testPatient.setAddress("123 Test St");
        testPatient.setPhoneNumber("123-456-7890");
    }

    @Test
    void testGetAllPatients() {
        // Arrange
        List<Patient> patientList = new ArrayList<>();
        patientList.add(testPatient);
        when(patientRepository.findAll()).thenReturn(patientList);
        
        // Act
        List<Patient> result = patientService.getAllPatients();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testPatient.getId(), result.get(0).getId());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById() {
        // Arrange
        when(patientRepository.findById(anyInt())).thenReturn(Optional.of(testPatient));
        
        // Act
        Optional<Patient> result = patientService.getPatientById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPatient.getId(), result.get().getId());
        verify(patientRepository, times(1)).findById(1);
    }

    @Test
    void testGetPatientByIdNotFound() {
        // Arrange
        when(patientRepository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act
        Optional<Patient> result = patientService.getPatientById(999);
        
        // Assert
        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findById(999);
    }

    @Test
    void testGetPatientByUser() {
        // Arrange
        when(patientRepository.findByUser(any(User.class))).thenReturn(Optional.of(testPatient));
        
        // Act
        Optional<Patient> result = patientService.getPatientByUser(testUser);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPatient.getId(), result.get().getId());
        verify(patientRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetPatientByUserNotFound() {
        // Arrange
        when(patientRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        
        // Act
        Optional<Patient> result = patientService.getPatientByUser(testUser);
        
        // Assert
        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findByUser(testUser);
    }
    
    @Test
    void testRegisterPatient() {
        // Arrange
        when(userService.registerUser(any(User.class), any(Role.RoleName.class))).thenReturn(testUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        
        Patient newPatient = new Patient();
        newPatient.setDateOfBirth(LocalDate.of(1995, 5, 15));
        newPatient.setGender("Female");
        
        // Act
        Patient result = patientService.registerPatient(newPatient, newUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testPatient.getId(), result.getId());
        assertEquals(testUser, result.getUser());
        verify(userService, times(1)).registerUser(newUser, Role.RoleName.PATIENT);
        verify(patientRepository, times(1)).save(newPatient);
    }
    
    @Test
    void testUpdatePatient() {
        // Arrange
        when(patientRepository.findById(anyInt())).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        
        // Act
        Patient result = patientService.updatePatient(testPatient);
        
        // Assert
        assertNotNull(result);
        assertEquals(testPatient.getId(), result.getId());
        verify(patientRepository, times(1)).findById(testPatient.getId());
        verify(patientRepository, times(1)).save(testPatient);
    }
    
    @Test
    void testUpdatePatientNotFound() {
        // Arrange
        Patient nonExistentPatient = new Patient();
        nonExistentPatient.setId(999);
        
        when(patientRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            patientService.updatePatient(nonExistentPatient);
        });
        
        // Verify message contains the ID
        assertTrue(exception.getMessage().contains("999"));
        verify(patientRepository, times(1)).findById(999);
        verify(patientRepository, never()).save(any(Patient.class));
    }
    
    @Test
    void testDeletePatient() {
        // Arrange
        doNothing().when(patientRepository).deleteById(anyInt());
        
        // Act
        patientService.deletePatient(1);
        
        // Assert
        verify(patientRepository, times(1)).deleteById(1);
    }
} 