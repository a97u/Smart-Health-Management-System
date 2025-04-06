package com.hospital.repository;

import com.hospital.entity.Patient;
import com.hospital.entity.PatientHealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientHealthMetricRepository extends JpaRepository<PatientHealthMetric, Integer> {
    List<PatientHealthMetric> findByPatient(Patient patient);
    List<PatientHealthMetric> findByPatientOrderByRecordDateAsc(Patient patient);
    List<PatientHealthMetric> findByPatientOrderByRecordDateDesc(Patient patient);
    List<PatientHealthMetric> findByPatientAndMetricTypeOrderByMeasurementDateDesc(Patient patient, String metricType);
    List<PatientHealthMetric> findByPatientAndMetricTypeAndMeasurementDateBetweenOrderByMeasurementDateAsc(
            Patient patient, String metricType, LocalDate startDate, LocalDate endDate);
} 