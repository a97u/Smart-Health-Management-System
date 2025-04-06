package com.hospital.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_health_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientHealthMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    @ManyToOne
    @JoinColumn(name = "record_id")
    private MedicalRecord medicalRecord;
    
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    @Column(name = "measurement_date")
    private LocalDate measurementDate;
    
    @Column(name = "metric_type", length = 50)
    private String metricType;
    
    @Column(name = "value", length = 50)
    private String value;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "blood_pressure")
    private Integer bloodPressure;
    
    @Column(name = "heart_rate")
    private Integer heartRate;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.recordDate == null) {
            this.recordDate = LocalDate.now();
        }
        if (this.measurementDate == null) {
            this.measurementDate = this.recordDate;
        }
    }
} 