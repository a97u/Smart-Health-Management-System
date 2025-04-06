package com.hospital.controller;

import com.hospital.entity.Document;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.service.DocumentService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Lightweight REST API controller for document management
 * Only includes essential endpoints for:
 * 1) Patients to upload documents
 * 2) Doctors to view patient documents
 * 3) Both to download documents
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    
    @Autowired
    private DocumentService documentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    /**
     * Patient only - Upload a document
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("documentName") String documentName,
            @RequestParam("documentType") String documentType,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Patient patient = patientService.getPatientByUser(user)
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));
            
            // Upload document for this patient (no medical record association)
            Document document = documentService.uploadDocument(patient, null, documentName, documentType, user, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("document", document);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error uploading document", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Doctor view of patient documents
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getPatientDocuments(
            @PathVariable Integer patientId,
            Authentication authentication) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            List<Document> documents = documentService.getDocumentsByPatient(patient);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("documents", documents);
            response.put("patient", patient);
            response.put("total", documents.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching documents for patient {}", patientId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving documents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Download document - for both patients and doctors
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            User user = userService.getUserByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Document document = documentService.getDocumentById(id)
                    .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
            
            // Security check - Patient can only access their own documents
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
                Patient patient = patientService.getPatientByUser(user)
                        .orElseThrow(() -> new RuntimeException("Patient profile not found"));
                
                if (!document.getPatient().getId().equals(patient.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            // Get document data from service
            byte[] documentData = documentService.getDocumentContent(document);
            
            // Create a ByteArrayResource from the document data
            ByteArrayResource resource = new ByteArrayResource(documentData);
            
            // Set up the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getDocumentName() + "\"");
            
            // Determine content type based on filename or default to octet-stream
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (document.getDocumentName().toLowerCase().endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if (document.getDocumentName().toLowerCase().endsWith(".jpg") || 
                       document.getDocumentName().toLowerCase().endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (document.getDocumentName().toLowerCase().endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            }
            
            // Return the response entity with the document data
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(documentData.length)
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading document with id {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
