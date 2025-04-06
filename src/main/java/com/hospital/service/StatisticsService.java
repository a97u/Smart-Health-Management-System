package com.hospital.service;

import com.hospital.entity.*;
import com.hospital.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating statistics and charts for reporting purposes
 */
@Service
public class StatisticsService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Generates a map of statistics for the admin dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // Count users by role
        long doctorCount = countDoctors();
        long nurseCount = countNurses();
        long patientCount = countPatients();
        long adminCount = countAdmins();

        // Add counts to statistics
        statistics.put("doctorCount", doctorCount);
        statistics.put("nurseCount", nurseCount);
        statistics.put("patientCount", patientCount);
        statistics.put("adminCount", adminCount);

        // Appointment statistics
        long totalAppointments = appointmentRepository.count();
        statistics.put("totalAppointments", totalAppointments);

        // Today's statistics
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentRepository.findByAppointmentDate(today);
        statistics.put("todayAppointments", todayAppointments.size());

        // Appointment status distribution
        Map<String, Long> appointmentStatusCounts = new HashMap<>();
        for (Appointment.AppointmentStatus status : Appointment.AppointmentStatus.values()) {
            long count = appointmentRepository.findByStatus(status).size();
            appointmentStatusCounts.put(status.name(), count);
        }
        statistics.put("appointmentStatusCounts", appointmentStatusCounts);

        // Get patients by blood group
        Map<String, Long> patientsByBloodGroup = getPatientsByBloodGroup();
        statistics.put("patientsByBloodGroup", patientsByBloodGroup);

        return statistics;
    }

    /**
     * Helper method to count doctors
     */
    private long countDoctors() {
        return doctorRepository.count();
    }

    /**
     * Helper method to count nurses
     */
    private long countNurses() {
        return nurseRepository.count();
    }

    /**
     * Helper method to count patients
     */
    private long countPatients() {
        return patientRepository.count();
    }

    /**
     * Helper method to count admins
     */
    private long countAdmins() {
        Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(adminRole))
                .count();
    }

    /**
     * Helper method to get patients grouped by blood group
     */
    private Map<String, Long> getPatientsByBloodGroup() {
        List<Patient> patients = patientRepository.findAll();
        
        return patients.stream()
                .filter(patient -> patient.getBloodGroup() != null && !patient.getBloodGroup().isEmpty())
                .collect(Collectors.groupingBy(
                    Patient::getBloodGroup,
                    Collectors.counting()
                ));
    }
} 