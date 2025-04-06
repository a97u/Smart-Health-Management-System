package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.DoctorRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor testDoctor;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("doctor@example.com");
        testUser.setName("Dr. Test");

        testDoctor = new Doctor();
        testDoctor.setId(1);
        testDoctor.setUser(testUser);
        testDoctor.setSpecialization("Cardiology");
    }

    @Test
    void testRegisterDoctor() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("newdoctor@example.com");
        newUser.setPassword("password");
        
        Doctor newDoctor = new Doctor();
        newDoctor.setSpecialization("Neurology");
        
        when(userService.registerUser(any(User.class), any(Role.RoleName.class))).thenReturn(testUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);
        
        // Act
        Doctor result = doctorService.registerDoctor(newDoctor, newUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testDoctor.getId(), result.getId());
        assertEquals(testUser, result.getUser());
        verify(userService, times(1)).registerUser(newUser, Role.RoleName.DOCTOR);
        verify(doctorRepository, times(1)).save(newDoctor);
    }

    @Test
    void testGetAllDoctors() {
        // Arrange
        List<Doctor> doctorList = new ArrayList<>();
        doctorList.add(testDoctor);
        when(doctorRepository.findAll()).thenReturn(doctorList);
        
        // Act
        List<Doctor> result = doctorService.getAllDoctors();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testDoctor.getId(), result.get(0).getId());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    void testGetDoctorById() {
        // Arrange
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.of(testDoctor));
        
        // Act
        Optional<Doctor> result = doctorService.getDoctorById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDoctor.getId(), result.get().getId());
        verify(doctorRepository, times(1)).findById(1);
    }

    @Test
    void testGetDoctorByIdNotFound() {
        // Arrange
        when(doctorRepository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act
        Optional<Doctor> result = doctorService.getDoctorById(999);
        
        // Assert
        assertFalse(result.isPresent());
        verify(doctorRepository, times(1)).findById(999);
    }

    @Test
    void testGetDoctorByUser() {
        // Arrange
        when(doctorRepository.findByUser(any(User.class))).thenReturn(Optional.of(testDoctor));
        
        // Act
        Optional<Doctor> result = doctorService.getDoctorByUser(testUser);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDoctor.getId(), result.get().getId());
        verify(doctorRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetDoctorByEmail() {
        // Arrange
        when(doctorRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testDoctor));
        
        // Act
        Optional<Doctor> result = doctorService.getDoctorByEmail("doctor@example.com");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDoctor.getId(), result.get().getId());
        verify(doctorRepository, times(1)).findByUserEmail("doctor@example.com");
    }

    @Test
    void testUpdateDoctor() {
        // Arrange
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);
        
        Doctor updatedDoctor = new Doctor();
        updatedDoctor.setId(1);
        updatedDoctor.setUser(testUser);
        updatedDoctor.setSpecialization("Updated Specialization");
        
        // Act
        Doctor result = doctorService.updateDoctor(updatedDoctor);
        
        // Assert
        assertNotNull(result);
        assertEquals(testDoctor.getId(), result.getId());
        verify(doctorRepository, times(1)).save(updatedDoctor);
    }

    @Test
    void testDeleteDoctor() {
        // Arrange
        doNothing().when(doctorRepository).deleteById(anyInt());
        
        // Act
        doctorService.deleteDoctor(1);
        
        // Assert
        verify(doctorRepository, times(1)).deleteById(1);
    }
} 