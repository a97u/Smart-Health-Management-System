package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Creates or updates an appointment
     */
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        if (appointment.getCreatedAt() == null) {
            appointment.setCreatedAt(LocalDateTime.now());
        }
        
        if (appointment.getId() != null) {
            // This is an update, so let's maintain creation data
            Optional<Appointment> existingAppointment = appointmentRepository.findById(appointment.getId());
            if (existingAppointment.isPresent()) {
                // Maintain creation data
                if (appointment.getCreatedAt() == null) {
                    appointment.setCreatedAt(existingAppointment.get().getCreatedAt());
                }
                if (appointment.getCreatedBy() == null) {
                    appointment.setCreatedBy(existingAppointment.get().getCreatedBy());
                }
                // Set updated at timestamp
                appointment.setUpdatedAt(LocalDateTime.now());
            }
        }
        
        return appointmentRepository.save(appointment);
    }

    /**
     * Schedules a new appointment and sends email confirmation
     */
    @Transactional
    public Appointment scheduleAppointment(Patient patient, Doctor doctor, LocalDate appointmentDate, String notes, User createdBy) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setNotes(notes);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointment.setCreatedBy(createdBy);
        appointment.setCreatedAt(LocalDateTime.now());
        
        // Save first
        appointment = appointmentRepository.save(appointment);
        
        // Then send email (but don't stop if email fails)
        try {
            emailService.sendAppointmentConfirmation(appointment);
        } catch (Exception e) {
            logger.warn("Failed to send appointment confirmation email", e);
        }
        
        return appointment;
    }

    /**
     * Retrieves all appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * Retrieves an appointment by ID
     */
    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Integer id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Retrieves appointments for a specific patient
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Patient patient) {
        return appointmentRepository.findByPatient(patient);
    }

    /**
     * Retrieves upcoming appointments for a specific patient with status SCHEDULED
     * 
     * @param patient The patient whose upcoming appointments to retrieve
     * @return List of upcoming scheduled appointments for the patient
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsForPatient(Patient patient) {
        return appointmentRepository.findByPatientAndStatusOrderByAppointmentDateAsc(
            patient, Appointment.AppointmentStatus.SCHEDULED);
    }

    /**
     * Cancels an appointment and sends cancellation email
     */
    @Transactional
    public Appointment cancelAppointment(Integer appointmentId, String cancelledBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointment.setUpdatedBy(cancelledBy);
            appointment = appointmentRepository.save(appointment);
            
            // Send cancellation email but don't stop if it fails
            try {
                emailService.sendAppointmentCancellation(appointment);
            } catch (Exception e) {
                logger.warn("Failed to send appointment cancellation email", e);
            }
            
            return appointment;
        }
        throw new RuntimeException("Appointment not found");
    }

    /**
     * Marks an appointment as completed
     */
    @Transactional
    public Appointment completeAppointment(Integer appointmentId, String completedBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointment.setUpdatedBy(completedBy);
            return appointmentRepository.save(appointment);
        }
        throw new RuntimeException("Appointment not found");
    }

    /**
     * Reschedules an appointment
     */
    @Transactional
    public Appointment rescheduleAppointment(Integer appointmentId, LocalDate newDate, String updatedBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            
            // Verify appointment is still scheduled
            if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
                throw new IllegalStateException("Cannot reschedule a non-scheduled appointment");
            }
            
            // Update with new date
            appointment.setAppointmentDate(newDate);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointment.setUpdatedBy(updatedBy);
            appointment = appointmentRepository.save(appointment);
            
            // Send update notification
            try {
                emailService.sendAppointmentConfirmation(appointment);
            } catch (Exception e) {
                logger.warn("Failed to send appointment update email", e);
            }
            
            return appointment;
        }
        throw new RuntimeException("Appointment not found");
    }

    /**
     * Retrieves upcoming appointments (today and future with SCHEDULED status)
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments() {
        // Since we don't have a direct repository method, we'll filter in memory
        LocalDate today = LocalDate.now();
        return appointmentRepository.findAll().stream()
            .filter(a -> (a.getAppointmentDate().isEqual(today) || 
                          a.getAppointmentDate().isAfter(today)) &&
                          a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves upcoming appointments for a specific doctor
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsForDoctor(Doctor doctor) {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByDoctor(doctor).stream()
            .filter(a -> (a.getAppointmentDate().isEqual(today) || 
                          a.getAppointmentDate().isAfter(today)) &&
                          a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves and sends reminders for tomorrow's appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTomorrowAppointments() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> appointments = appointmentRepository.findByAppointmentDate(tomorrow).stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
            .collect(Collectors.toList());
        
        // Send reminders for tomorrow's appointments
        for (Appointment appointment : appointments) {
            try {
                emailService.sendAppointmentReminder(appointment);
            } catch (Exception e) {
                logger.warn("Failed to send appointment reminder email", e);
            }
        }
        
        return appointments;
    }

    /**
     * Retrieves appointments for a specific doctor
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    /**
     * Retrieves appointments for a specific date
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date);
    }

    /**
     * Retrieves appointments with a specific status
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    /**
     * Updates the status of an appointment
     */
    @Transactional
    public Appointment updateAppointmentStatus(Integer id, Appointment.AppointmentStatus status, String updatedBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus(status);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointment.setUpdatedBy(updatedBy);
            
            // Send appropriate notifications based on status
            try {
                if (status == Appointment.AppointmentStatus.CANCELLED) {
                    emailService.sendAppointmentCancellation(appointment);
                } else if (status == Appointment.AppointmentStatus.SCHEDULED) {
                    emailService.sendAppointmentConfirmation(appointment);
                }
            } catch (Exception e) {
                logger.warn("Failed to send status update email", e);
            }
            
            return appointmentRepository.save(appointment);
        }
        throw new RuntimeException("Appointment not found with id: " + id);
    }

    /**
     * Deletes an appointment
     * NOTE: In production, consider soft delete instead
     */
    @Transactional
    public void deleteAppointment(Integer id) {
        appointmentRepository.deleteById(id);
    }

    /**
     * Retrieves today's appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointments() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByAppointmentDate(today);
    }
    
    /**
     * Retrieves today's appointments for a specific doctor
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointmentsForDoctor(Doctor doctor) {
        LocalDate today = LocalDate.now();
        // Use the repository method directly
        return appointmentRepository.findByDoctorAndAppointmentDate(doctor, today);
    }
    
    /**
     * Validates if an appointment conflicts with existing appointments
     * @param doctorId The doctor's ID
     * @param date The proposed appointment date
     * @param existingAppointmentId Optional ID of an existing appointment (to exclude from check)
     * @return true if there's a conflict, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasAppointmentConflict(Integer doctorId, LocalDate date, Integer existingAppointmentId) {
        // Use the repository method to find scheduled appointments for this doctor on this date
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatus(
                doctorId, date, Appointment.AppointmentStatus.SCHEDULED);
        
        // If we're updating an existing appointment, exclude it from the conflict check
        if (existingAppointmentId != null) {
            existingAppointments = existingAppointments.stream()
                    .filter(a -> !a.getId().equals(existingAppointmentId))
                    .collect(Collectors.toList());
        }
        
        return !existingAppointments.isEmpty();
    }

    /**
     * Retrieves upcoming appointments for a specific patient (now and future with SCHEDULED status)
     */
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsByPatient(Patient patient) {
        LocalDate today = LocalDate.now();
        List<Appointment> scheduledAppointments = appointmentRepository.findByPatientAndStatusOrderByAppointmentDateAsc(
                patient, Appointment.AppointmentStatus.SCHEDULED);
        
        // Filter to only include today and future appointments
        return scheduledAppointments.stream()
                .filter(a -> !a.getAppointmentDate().isBefore(today))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves past appointments for a specific patient (before today or with COMPLETED/CANCELLED status)
     */
    @Transactional(readOnly = true)
    public List<Appointment> getPastAppointmentsByPatient(Patient patient) {
        LocalDate today = LocalDate.now();
        List<Appointment> allAppointments = appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
        
        // Include appointments with past dates or completed/cancelled status
        return allAppointments.stream()
                .filter(a -> a.getAppointmentDate().isBefore(today) || 
                        a.getStatus() == Appointment.AppointmentStatus.COMPLETED ||
                        a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());
    }
}