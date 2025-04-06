package com.hospital.service;

import com.hospital.entity.Appointment;
import com.hospital.entity.Patient;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserService userService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private MedicalRecordService medicalRecordService;
    
    @Autowired
    private PatientHealthMetricService healthMetricService;

    // --- CRUD Operations ---

    @Transactional
    public Patient registerPatient(Patient patient, User user) {
        try {
            logger.info("Registering new patient with email: {}", user.getEmail());
            
            // Check if user already exists
            if (userService.existsByEmail(user.getEmail())) {
                logger.warn("Cannot register patient. User with email {} already exists", user.getEmail());
                throw new IllegalArgumentException("User with this email already exists");
            }
            
            // Register user account with PATIENT role
            User savedUser = userService.registerUser(user, Role.RoleName.PATIENT);
            patient.setUser(savedUser);
            
            // Set default values for patient if not provided
            if (patient.getDateOfBirth() == null) {
                // Default to 18 years old if not provided
                patient.setDateOfBirth(LocalDate.now().minusYears(18));
            }
            
            Patient savedPatient = patientRepository.save(patient);
            
            // Email confirmation could be added here if needed
            logger.info("Patient registered successfully with ID: {}", savedPatient.getId());
            
            return savedPatient;
        } catch (Exception e) {
            logger.error("Error registering patient: ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        try {
            return patientRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all patients: ", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Patient> getPatientById(Integer id) {
        try {
            return patientRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving patient by ID {}: ", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByUser(User user) {
        try {
            return patientRepository.findByUser(user);
        } catch (Exception e) {
            logger.error("Error retrieving patient by user ID {}: ", user.getId(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByEmail(String email) {
        try {
            return patientRepository.findByUserEmail(email);
        } catch (Exception e) {
            logger.error("Error retrieving patient by email {}: ", email, e);
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByPhoneNumber(String phoneNumber) {
        try {
            return patientRepository.findByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            logger.error("Error retrieving patient by phone number: ", e);
            throw e;
        }
    }

    @Transactional
    public Patient updatePatient(Patient patient) {
        try {
            logger.info("Updating patient with ID: {}", patient.getId());
            
            // Get existing patient to preserve any fields that shouldn't be updated
            Optional<Patient> existingPatientOpt = patientRepository.findById(patient.getId());
            if (existingPatientOpt.isPresent()) {
                Patient existingPatient = existingPatientOpt.get();
                
                // Maintain the User relationship - user should be updated separately
                patient.setUser(existingPatient.getUser());
                
                return patientRepository.save(patient);
            } else {
                logger.warn("Cannot update patient with ID {}. Patient not found.", patient.getId());
                throw new IllegalArgumentException("Patient not found with ID: " + patient.getId());
            }
        } catch (Exception e) {
            logger.error("Error updating patient: ", e);
            throw e;
        }
    }

    @Transactional
    public void deletePatient(Integer id) {
        try {
            logger.info("Deleting patient with ID: {}", id);
            patientRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error deleting patient with ID {}: ", id, e);
            throw e;
        }
    }
    
    
    // --- Dashboard and Statistics ---
    
    @Transactional(readOnly = true)
    public Map<String, Object> getPatientStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // Total patients
            statistics.put("totalPatients", patientRepository.count());
            
            // Patients by blood group
            Map<String, Long> bloodGroupCounts = new HashMap<>();
            
            // Get all patients and count them by blood group
            List<Patient> allPatients = patientRepository.findAll();
            Map<String, Long> bloodGroups = allPatients.stream()
                .filter(p -> p.getBloodGroup() != null && !p.getBloodGroup().isEmpty())
                .collect(Collectors.groupingBy(
                    Patient::getBloodGroup, 
                    Collectors.counting()
                ));
                
            statistics.put("patientsByBloodGroup", bloodGroups);
            
            // New patients this month
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            List<Patient> newPatients = patientRepository.findAll().stream()
                    .filter(p -> p.getUser() != null && p.getUser().getCreatedAt() != null)
                    .filter(p -> {
                        LocalDateTime createdAt = p.getUser().getCreatedAt();
                        LocalDate createdDate = createdAt.toLocalDate();
                        return createdDate.isEqual(firstDayOfMonth) || createdDate.isAfter(firstDayOfMonth);
                    })
                    .collect(Collectors.toList());
            
            statistics.put("newPatientsThisMonth", newPatients.size());
            
        } catch (Exception e) {
            logger.error("Error generating patient statistics: ", e);
            // Add default values for required statistics
            statistics.put("totalPatients", 0L);
            statistics.put("patientsByBloodGroup", new HashMap<>());
            statistics.put("newPatientsThisMonth", 0);
        }
        
        return statistics;
    }
    
    // --- Patient Dashboard Data ---
    
    @Transactional(readOnly = true)
    public Map<String, Object> getPatientDashboardData(Patient patient) {
        Map<String, Object> dashboardData = new HashMap<>();
        
        try {
            // Add user information
            dashboardData.put("patient", patient);
            dashboardData.put("user", patient.getUser());
            
            // Get appointment data from AppointmentService
            List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patient);
            
            // Filter upcoming appointments
            List<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointmentsForPatient(patient);
            
            dashboardData.put("upcomingAppointments", upcomingAppointments.size());
            dashboardData.put("upcomingAppointmentsList", upcomingAppointments);
            
            // Get medical records count
            int medicalRecordsCount = medicalRecordService.getMedicalRecordsByPatient(patient).size();
            dashboardData.put("totalRecords", medicalRecordsCount);
            
            // Add health metrics data
            Map<String, Object> healthMetricsData = healthMetricService.getPatientHealthMetricsChartData(patient);
            dashboardData.put("healthMetrics", healthMetricsData);
            
            // Get latest values for common metrics from chart data
            try {
                Double[] weights = (Double[]) healthMetricsData.get("weights");
                if (weights != null && weights.length > 0) {
                    dashboardData.put("latestWeight", weights[weights.length - 1]);
                }
            } catch (Exception e) {
                logger.warn("Error getting latest weight for patient {}: {}", patient.getId(), e.getMessage());
            }
            
            try {
                String[] bloodPressures = (String[]) healthMetricsData.get("bloodPressures");
                if (bloodPressures != null && bloodPressures.length > 0) {
                    String bpString = bloodPressures[bloodPressures.length - 1];
                    // For backward compatibility with code expecting integer value
                    if (bpString != null && !bpString.contains("/")) {
                        try {
                            int bpValue = Integer.parseInt(bpString.trim());
                            dashboardData.put("latestBloodPressure", bpValue);
                        } catch (NumberFormatException nfe) {
                            // Just use the string value
                            dashboardData.put("latestBloodPressureText", bpString);
                        }
                    } else {
                        dashboardData.put("latestBloodPressureText", bpString);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error getting latest blood pressure for patient {}: {}", patient.getId(), e.getMessage());
            }
            
            try {
                Integer[] heartRates = (Integer[]) healthMetricsData.get("heartRates");
                if (heartRates != null && heartRates.length > 0) {
                    dashboardData.put("latestHeartRate", heartRates[heartRates.length - 1]);
                }
            } catch (Exception e) {
                logger.warn("Error getting latest heart rate for patient {}: {}", patient.getId(), e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error generating patient dashboard data for patient {}: ", patient.getId(), e);
            // Ensure minimum data is available
            dashboardData.put("patient", patient);
            dashboardData.put("upcomingAppointments", 0);
            dashboardData.put("upcomingAppointmentsList", List.of());
            dashboardData.put("totalRecords", 0);
        }
        
        return dashboardData;
    }
}