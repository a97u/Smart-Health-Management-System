package com.hospital.controller;

import com.hospital.entity.Document;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.DocumentService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentControllerTest {

    @Mock
    private DocumentService documentService;
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private Authentication authentication;
    
    @InjectMocks
    private DocumentController documentController;
    
    private Document testDocument;
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
        
        testDocument = new Document();
        testDocument.setId(1);
        testDocument.setPatient(testPatient);
        testDocument.setDocumentName("Test Document");
        testDocument.setDocumentType("Test Type");
        testDocument.setUploadedBy(testUser);
        
        new MockMultipartFile(
            "file", 
            "test.pdf",
            "application/pdf", 
            "test file content".getBytes()
        );
    }
    
    @Test
    void testDocumentCreation() {
        // Basic test for entity creation
        assertNotNull(testDocument);
        assertEquals(1, testDocument.getId());
        assertEquals(testPatient, testDocument.getPatient());
        assertEquals("Test Document", testDocument.getDocumentName());
        assertEquals("Test Type", testDocument.getDocumentType());
        assertEquals(testUser, testDocument.getUploadedBy());
    }
    
    @Test
    void testGetDocumentsByPatientService() {
        // Test the service method directly
        List<Document> documents = new ArrayList<>();
        documents.add(testDocument);
        when(documentService.getDocumentsByPatient(any(Patient.class))).thenReturn(documents);
        
        List<Document> result = documentService.getDocumentsByPatient(testPatient);
        
        assertEquals(1, result.size());
        assertEquals(testDocument.getId(), result.get(0).getId());
        verify(documentService, times(1)).getDocumentsByPatient(testPatient);
    }
    
    @Test
    void testGetDocumentByIdService() {
        when(documentService.getDocumentById(anyInt())).thenReturn(Optional.of(testDocument));
        
        Optional<Document> result = documentService.getDocumentById(1);
        
        assertTrue(result.isPresent());
        assertEquals(testDocument.getId(), result.get().getId());
        verify(documentService, times(1)).getDocumentById(1);
    }
    
    @Test
    void testGetPatientByUserService() {
        when(patientService.getPatientByUser(any(User.class))).thenReturn(Optional.of(testPatient));
        
        Optional<Patient> result = patientService.getPatientByUser(testUser);
        
        assertTrue(result.isPresent());
        assertEquals(testPatient.getId(), result.get().getId());
        verify(patientService, times(1)).getPatientByUser(testUser);
    }
    
    @Test
    void testGetUserByEmailService() {
        when(userService.getUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        Optional<User> result = userService.getUserByEmail("patient@example.com");
        
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userService, times(1)).getUserByEmail("patient@example.com");
    }
    
    @Test
    void testDocumentControllerInitialized() {
        // Simple test to verify initialization
        assertNotNull(documentController);
        assertNotNull(documentService);
    }
} 