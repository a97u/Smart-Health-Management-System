package com.hospital.controller;

import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.PatientHealthMetric;
import com.hospital.entity.User;
import com.hospital.repository.PatientHealthMetricRepository;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientHealthMetricService;
import com.hospital.service.PatientService;
import com.hospital.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/health-metrics")
public class PatientHealthMetricController {

    private static final Logger logger = LoggerFactory.getLogger(PatientHealthMetricController.class);

    @Autowired
    private PatientHealthMetricService healthMetricService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PatientHealthMetricRepository healthMetricRepository;
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<List<PatientHealthMetric>> getPatientHealthMetrics(@PathVariable Integer patientId) {
        try {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
        
        List<PatientHealthMetric> metrics = healthMetricService.getHealthMetricsByPatient(patient);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Error fetching health metrics for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/patient/{patientId}/chart-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<Map<String, Object>> getPatientHealthMetricsChartData(@PathVariable Integer patientId) {
        try {
        Patient patient = patientService.getPatientById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
        
            Map<String, Object> chartData = healthMetricService.getPatientHealthMetricsChartData(patient);
            
            // Add chart URLs to maintain visualization compatibility
            chartData.put("weightChartUrl", "/api/health-metrics/chart/" + patientId + "/weight");
            chartData.put("bpChartUrl", "/api/health-metrics/chart/" + patientId + "/blood-pressure");
            chartData.put("hrChartUrl", "/api/health-metrics/chart/" + patientId + "/heart-rate");
            
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            logger.error("Error fetching chart data for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<Map<String, Object>> addHealthMetric(
            @PathVariable Integer patientId,
            @RequestBody PatientHealthMetric healthMetric) {
        
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            // Set patient on the health metric
            healthMetric.setPatient(patient);
            
            // Set date to today if not provided
            if (healthMetric.getRecordDate() == null) {
                healthMetric.setRecordDate(LocalDate.now());
            }
            
            // Save the health metric
            PatientHealthMetric savedMetric = healthMetricService.createHealthMetric(healthMetric);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Health metric added successfully");
            response.put("healthMetric", savedMetric);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error adding health metric for patient {}", patientId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    // Chart generation endpoints with direct JFreeChart image output
    
    @GetMapping(value = "/chart/{patientId}/weight", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<byte[]> getWeightChart(@PathVariable Integer patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            // Generate the chart using JFreeChart
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);
            String chartUrl = healthMetricService.generateMetricChart(patient, "WEIGHT", startDate, endDate);
            
            if (chartUrl == null) {
                logger.error("Failed to generate weight chart for patient {}", patientId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Extract file path from URL
            String filePath = chartUrl.substring(0, chartUrl.indexOf("?")).replace("/images", "src/main/resources/static/images");
            File chartFile = new File(filePath);
            
            if (!chartFile.exists()) {
                logger.error("Chart file does not exist: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Read the file into a byte array
            byte[] imageBytes = Files.readAllBytes(chartFile.toPath());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (IOException e) {
            logger.error("Error reading chart file for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error generating weight chart for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping(value = "/chart/{patientId}/blood-pressure", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<byte[]> getBloodPressureChart(@PathVariable Integer patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            // Generate the chart using JFreeChart
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);
            String chartUrl = healthMetricService.generateMetricChart(patient, "BLOOD_PRESSURE", startDate, endDate);
            
            if (chartUrl == null) {
                logger.error("Failed to generate blood pressure chart for patient {}", patientId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Extract file path from URL
            String filePath = chartUrl.substring(0, chartUrl.indexOf("?")).replace("/images", "src/main/resources/static/images");
            File chartFile = new File(filePath);
            
            if (!chartFile.exists()) {
                logger.error("Chart file does not exist: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Read the file into a byte array
            byte[] imageBytes = Files.readAllBytes(chartFile.toPath());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (IOException e) {
            logger.error("Error reading chart file for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error generating blood pressure chart for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping(value = "/chart/{patientId}/heart-rate", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'NURSE') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<byte[]> getHeartRateChart(@PathVariable Integer patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            // Generate the chart using JFreeChart
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusYears(1);
            String chartUrl = healthMetricService.generateMetricChart(patient, "HEART_RATE", startDate, endDate);
            
            if (chartUrl == null) {
                logger.error("Failed to generate heart rate chart for patient {}", patientId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // Extract file path from URL
            String filePath = chartUrl.substring(0, chartUrl.indexOf("?")).replace("/images", "src/main/resources/static/images");
            File chartFile = new File(filePath);
            
            if (!chartFile.exists()) {
                logger.error("Chart file does not exist: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Read the file into a byte array
            byte[] imageBytes = Files.readAllBytes(chartFile.toPath());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (IOException e) {
            logger.error("Error reading chart file for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error generating heart rate chart for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Simplify the chart visualization endpoint for lightweight Postman testing
    @GetMapping("/visualize/{patientId}")
    public ResponseEntity<Map<String, Object>> visualizeHealthMetrics(@PathVariable Integer patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            // Just return URLs to the chart images for Postman testing
            Map<String, Object> response = new HashMap<>();
            response.put("patient", patient);
            response.put("weightChartUrl", "/api/health-metrics/chart/" + patientId + "/weight");
            response.put("bloodPressureChartUrl", "/api/health-metrics/chart/" + patientId + "/blood-pressure");
            response.put("heartRateChartUrl", "/api/health-metrics/chart/" + patientId + "/heart-rate");
            response.put("htmlVisualizationUrl", "/api/health-metrics/visualize/" + patientId + "/html");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating visualization for patient {}", patientId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Add a simple HTML endpoint for browser testing
    @GetMapping(value = "/visualize/{patientId}/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> visualizeHealthMetricsHtml(@PathVariable Integer patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));
            
            String htmlContent = generateSimpleVisualizationHtml(patient);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);
        } catch (Exception e) {
            logger.error("Error generating HTML visualization for patient {}", patientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("<html><body><h1>Error</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    private String generateSimpleVisualizationHtml(Patient patient) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n")
            .append("<head>\n")
            .append("    <title>Health Metrics Charts</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial; margin: 20px; }\n")
            .append("        .chart { margin: 20px 0; }\n")
            .append("        img { max-width: 100%; border: 1px solid #ddd; }\n")
            .append("    </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <h1>Health Metrics for Patient ID: ").append(patient.getId()).append("</h1>\n")
            .append("    <div class=\"chart\">\n")
            .append("        <h2>Weight Chart</h2>\n")
            .append("        <img src=\"/api/health-metrics/chart/").append(patient.getId()).append("/weight\" alt=\"Weight Chart\">\n")
            .append("    </div>\n")
            .append("    <div class=\"chart\">\n")
            .append("        <h2>Blood Pressure Chart</h2>\n")
            .append("        <img src=\"/api/health-metrics/chart/").append(patient.getId()).append("/blood-pressure\" alt=\"Blood Pressure Chart\">\n")
            .append("    </div>\n")
            .append("    <div class=\"chart\">\n")
            .append("        <h2>Heart Rate Chart</h2>\n")
            .append("        <img src=\"/api/health-metrics/chart/").append(patient.getId()).append("/heart-rate\" alt=\"Heart Rate Chart\">\n")
            .append("    </div>\n")
            .append("</body>\n")
            .append("</html>");
            
        return html.toString();
    }
} 