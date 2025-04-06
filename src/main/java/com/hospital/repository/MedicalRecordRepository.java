package com.hospital.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
	List<MedicalRecord> findByPatient(Patient patient);
    List<MedicalRecord> findByDoctor(Doctor doctor);
    List<MedicalRecord> findByVisitDate(LocalDate visitDate);
    List<MedicalRecord> findByPatientOrderByVisitDateDesc(Patient patient);
}
