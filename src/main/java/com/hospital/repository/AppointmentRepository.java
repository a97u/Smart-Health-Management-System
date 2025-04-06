package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByPatient(Patient patient);
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByAppointmentDate(LocalDate date);
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate date);
    List<Appointment> findByDoctorIdAndAppointmentDate(Integer doctorId, LocalDate date);
    List<Appointment> findByPatientAndAppointmentDate(Patient patient, LocalDate date);
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(Integer doctorId, LocalDate date, Appointment.AppointmentStatus status);
    List<Appointment> findByPatientAndStatusOrderByAppointmentDateAsc(Patient patient, Appointment.AppointmentStatus status);
    List<Appointment> findByPatientOrderByAppointmentDateDesc(Patient patient);
}