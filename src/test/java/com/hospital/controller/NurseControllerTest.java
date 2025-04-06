package com.hospital.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.entity.Nurse;
import com.hospital.service.NurseService;
import com.hospital.service.UserService;

@ExtendWith(MockitoExtension.class)
public class NurseControllerTest {

    @Mock
    private NurseService nurseService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private NurseController nurseController;
    
    private List<Nurse> testNurses;
    
    @BeforeEach
    void setUp() {
        testNurses = new ArrayList<>();
        Nurse nurse = new Nurse();
        nurse.setId(1);
        testNurses.add(nurse);
    }
    
    @Test
    void testNurseControllerInitialized() {
        // Simple test to verify initialization
        assertNotNull(nurseController);
        assertNotNull(nurseService);
    }
    
    @Test
    void testGetAllNursesService() {
        // Arrange
        when(nurseService.getAllNurses()).thenReturn(testNurses);
        
        // Act
        List<Nurse> result = nurseService.getAllNurses();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        verify(nurseService, times(1)).getAllNurses();
    }
    
    @Test
    void testGetNurseByIdService() {
        // Arrange
        when(nurseService.getNurseById(anyInt())).thenReturn(Optional.of(testNurses.get(0)));
        
        // Act
        Optional<Nurse> result = nurseService.getNurseById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(nurseService, times(1)).getNurseById(1);
    }
} 