package com.hospital.repository;

import com.hospital.entity.Document;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
    List<Document> findByPatient(Patient patient);
    List<Document> findByRecord(MedicalRecord record);
    List<Document> findByDocumentType(String documentType);
}