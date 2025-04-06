package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        return medicalRecordRepository.save(medicalRecord);
    }

    @Transactional
    public MedicalRecord createMedicalRecord(Patient patient, Doctor doctor, LocalDate visitDate, String prescription) {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setVisitDate(visitDate);
        medicalRecord.setPrescription(prescription);
        return medicalRecordRepository.save(medicalRecord);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<MedicalRecord> getMedicalRecordById(Integer id) {
        return medicalRecordRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByPatient(Patient patient) {
        return medicalRecordRepository.findByPatient(patient);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByDoctor(Doctor doctor) {
        return medicalRecordRepository.findByDoctor(doctor);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByVisitDate(LocalDate visitDate) {
        return medicalRecordRepository.findByVisitDate(visitDate);
    }

    @Transactional
    public MedicalRecord updateMedicalRecord(MedicalRecord medicalRecord) {
        return medicalRecordRepository.save(medicalRecord);
    }

    @Transactional
    public void deleteMedicalRecord(Integer id) {
        medicalRecordRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getRecentMedicalRecordsByPatient(Patient patient, int limit) {
        List<MedicalRecord> allRecords = medicalRecordRepository.findByPatientOrderByVisitDateDesc(patient);
        
        // Return up to 'limit' records
        return allRecords.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
}