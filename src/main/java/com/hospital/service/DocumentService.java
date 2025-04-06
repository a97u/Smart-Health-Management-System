package com.hospital.service;

import com.hospital.entity.Document;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Transactional
    public Document uploadDocument(Patient patient, MedicalRecord record, String documentName, 
                                  String documentType, User uploadedBy, MultipartFile file) throws IOException {
        Document document = new Document();
        document.setPatient(patient);
        document.setRecord(record);
        document.setDocumentName(documentName);
        document.setDocumentType(documentType);
        document.setUploadedBy(uploadedBy);
        
        // Save the file binary data
        document.setFileData(file.getBytes());
        
        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Document> getDocumentById(Integer id) {
        return documentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByPatient(Patient patient) {
        return documentRepository.findByPatient(patient);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByRecord(MedicalRecord record) {
        return documentRepository.findByRecord(record);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByType(String documentType) {
        return documentRepository.findByDocumentType(documentType);
    }

    @Transactional
    public void deleteDocument(Integer id) {
        documentRepository.deleteById(id);
    }

    /**
     * Get document content as byte array
     */
    public byte[] getDocumentContent(Document document) throws IOException {
        // In a real implementation, this would likely retrieve the file from a storage system
        // For now, we'll return the file data stored in the document
        // Assuming the document entity has a field for binary data or a reference to where it's stored
        
        // This is a placeholder implementation. In a real application:
        // 1. You might store documents in a file system and retrieve them by path
        // 2. You might store documents in a database as BLOBs
        // 3. You might use cloud storage (S3, Google Cloud Storage, etc.)
        
        // For demonstration, return some dummy PDF data
        // In production, replace this with actual file retrieval logic
        if (document == null) {
            throw new IOException("Document not found");
        }
        
        // Get actual document content from the storage system
        // This is where you would implement file/blob retrieval
        return document.getFileData(); // Assuming Document entity has this field
    }
}