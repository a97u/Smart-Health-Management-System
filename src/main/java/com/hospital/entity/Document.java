package com.hospital.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "record_id")
    private MedicalRecord record;
    
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @Column(name = "document_name", nullable = false)
    private String documentName;
    
    @Column(name = "document_type", nullable = false)
    private String documentType;
    
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;
    
    @ManyToOne
    @JoinColumn(name = "upload_by", nullable = false)
    private User uploadedBy;
    
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;
    
    @PrePersist
    public void prePersist() {
        this.uploadDate = LocalDateTime.now();
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}