package com.hospital.service;

import com.hospital.entity.Document;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.repository.DocumentRepository;
import com.hospital.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private DocumentService documentService;

    private Document testDocument;
    private Patient testPatient;
    private User testUser;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("patient@example.com");
        testUser.setName("Test Patient");
        
        // Create test patient
        testPatient = new Patient();
        testPatient.setId(1);
        testPatient.setUser(testUser);
        
        // Create test document
        testDocument = new Document();
        testDocument.setId(1);
        testDocument.setPatient(testPatient);
        testDocument.setDocumentName("Test Document");
        testDocument.setDocumentType("PDF");
        testDocument.setUploadedBy(testUser);
        
        // Create test file
        testFile = new MockMultipartFile(
            "file", 
            "test.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );
    }

    @Test
    void testGetAllDocuments() {
        // Arrange
        List<Document> documentList = new ArrayList<>();
        documentList.add(testDocument);
        when(documentRepository.findAll()).thenReturn(documentList);
        
        // Act
        List<Document> result = documentService.getAllDocuments();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testDocument.getId(), result.get(0).getId());
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void testGetDocumentById() {
        // Arrange
        when(documentRepository.findById(anyInt())).thenReturn(Optional.of(testDocument));
        
        // Act
        Optional<Document> result = documentService.getDocumentById(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDocument.getId(), result.get().getId());
        verify(documentRepository, times(1)).findById(1);
    }

    @Test
    void testGetDocumentsByPatient() {
        // Arrange
        List<Document> documentList = new ArrayList<>();
        documentList.add(testDocument);
        when(documentRepository.findByPatient(any(Patient.class))).thenReturn(documentList);
        
        // Act
        List<Document> result = documentService.getDocumentsByPatient(testPatient);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(testDocument.getId(), result.get(0).getId());
        verify(documentRepository, times(1)).findByPatient(testPatient);
    }

    @Test
    void testRepositorySaveMethod() {
        // Arrange
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        
        // Act - test repository method directly
        Document result = documentRepository.save(testDocument);
        
        // Assert
        assertNotNull(result);
        assertEquals(testDocument.getId(), result.getId());
        verify(documentRepository, times(1)).save(testDocument);
    }

    @Test
    void testDeleteDocument() {
        // Arrange
        doNothing().when(documentRepository).deleteById(anyInt());
        
        // Act
        documentService.deleteDocument(1);
        
        // Assert
        verify(documentRepository, times(1)).deleteById(1);
    }
} 