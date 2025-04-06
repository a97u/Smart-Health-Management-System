package com.hospital.service;

import com.hospital.entity.Nurse;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.NurseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NurseServiceTest {

    @Mock
    private NurseRepository nurseRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NurseService nurseService;

    private Nurse testNurse;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("nurse@example.com");
        testUser.setName("Test Nurse");

        testNurse = new Nurse();
        testNurse.setId(1);
        testNurse.setUser(testUser);
        testNurse.setPhoneNumber("123-456-7890");
    }

    @Test
    void testGetAllNurses() {
        // Arrange
        List<Nurse> nurseList = new ArrayList<>();
        nurseList.add(testNurse);
        when(nurseRepository.findAll()).thenReturn(nurseList);

        // Act
        List<Nurse> result = nurseService.getAllNurses();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testNurse.getId(), result.get(0).getId());
        verify(nurseRepository, times(1)).findAll();
    }

    @Test
    void testGetNurseById() {
        // Arrange
        when(nurseRepository.findById(anyInt())).thenReturn(Optional.of(testNurse));

        // Act
        Optional<Nurse> result = nurseService.getNurseById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testNurse.getId(), result.get().getId());
        verify(nurseRepository, times(1)).findById(1);
    }

    @Test
    void testGetNurseByUser() {
        // Arrange
        when(nurseRepository.findByUser(any(User.class))).thenReturn(Optional.of(testNurse));

        // Act
        Optional<Nurse> result = nurseService.getNurseByUser(testUser);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testNurse.getId(), result.get().getId());
        verify(nurseRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testRegisterNurse() {
        // Arrange
        when(userService.registerUser(any(User.class), any(Role.RoleName.class))).thenReturn(testUser);
        when(nurseRepository.save(any(Nurse.class))).thenReturn(testNurse);

        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        Nurse newNurse = new Nurse();
        newNurse.setPhoneNumber("987-654-3210");

        // Act
        Nurse result = nurseService.registerNurse(newNurse, newUser);

        // Assert
        assertNotNull(result);
        assertEquals(testNurse.getId(), result.getId());
        assertEquals(testUser, result.getUser());
        verify(userService, times(1)).registerUser(newUser, Role.RoleName.NURSE);
        verify(nurseRepository, times(1)).save(newNurse);
    }

    @Test
    void testUpdateNurse() {
        // Arrange
        when(nurseRepository.save(any(Nurse.class))).thenReturn(testNurse);

        // Act
        Nurse result = nurseService.updateNurse(testNurse);

        // Assert
        assertNotNull(result);
        assertEquals(testNurse.getId(), result.getId());
        verify(nurseRepository, times(1)).save(testNurse);
    }

    @Test
    void testDeleteNurse() {
        // Arrange
        doNothing().when(nurseRepository).deleteById(anyInt());

        // Act
        nurseService.deleteNurse(1);

        // Assert
        verify(nurseRepository, times(1)).deleteById(1);
    }
} 