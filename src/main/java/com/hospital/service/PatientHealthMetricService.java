package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.MedicalRecord;
import com.hospital.entity.Patient;
import com.hospital.entity.PatientHealthMetric;
import com.hospital.repository.PatientHealthMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PatientHealthMetricService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientHealthMetricService.class);

    @Autowired
    private PatientHealthMetricRepository healthMetricRepository;

    @Transactional
    public PatientHealthMetric createHealthMetric(PatientHealthMetric healthMetric) {
        // Set measurement date if not provided
        if (healthMetric.getMeasurementDate() == null) {
            healthMetric.setMeasurementDate(healthMetric.getRecordDate() != null ? 
                    healthMetric.getRecordDate() : LocalDate.now());
        }
        
        // Set metricType based on what values are available
        if (healthMetric.getMetricType() == null) {
            if (healthMetric.getWeight() != null) {
                healthMetric.setMetricType("WEIGHT");
                healthMetric.setValue(String.valueOf(healthMetric.getWeight()));
            } else if (healthMetric.getBloodPressure() != null) {
                healthMetric.setMetricType("BLOOD_PRESSURE");
                healthMetric.setValue(String.valueOf(healthMetric.getBloodPressure()));
            } else if (healthMetric.getHeartRate() != null) {
                healthMetric.setMetricType("HEART_RATE");
                healthMetric.setValue(String.valueOf(healthMetric.getHeartRate()));
            }
        }
        
        logger.info("Creating health metric: type={}, value={}, measurementDate={}", 
                healthMetric.getMetricType(), healthMetric.getValue(), healthMetric.getMeasurementDate());
                
        return healthMetricRepository.save(healthMetric);
    }
    
    @Transactional
    public PatientHealthMetric recordHealthMetrics(
            Patient patient, 
            Doctor doctor, 
            MedicalRecord medicalRecord,
            LocalDate recordDate, 
            Double weight, 
            Integer bloodPressure,
            Integer heartRate,
            String notes) {
        
        PatientHealthMetric metric = new PatientHealthMetric();
        metric.setPatient(patient);
        metric.setDoctor(doctor);
        metric.setMedicalRecord(medicalRecord);
        metric.setRecordDate(recordDate != null ? recordDate : LocalDate.now());
        metric.setMeasurementDate(recordDate != null ? recordDate : LocalDate.now());
        
        // Store all values consistently
        if (weight != null) {
        metric.setWeight(weight);
            
            // Also record as a typed metric
            PatientHealthMetric weightMetric = new PatientHealthMetric();
            weightMetric.setPatient(patient);
            weightMetric.setDoctor(doctor);
            weightMetric.setMedicalRecord(medicalRecord);
            weightMetric.setRecordDate(metric.getRecordDate());
            weightMetric.setMeasurementDate(metric.getMeasurementDate());
            weightMetric.setMetricType("WEIGHT");
            weightMetric.setValue(String.valueOf(weight));
            weightMetric.setWeight(weight);
            weightMetric.setNotes("Weight measurement: " + weight + " kg");
            healthMetricRepository.save(weightMetric);
        }
        
        if (bloodPressure != null) {
        metric.setBloodPressure(bloodPressure);
            
            // Also record as a typed metric
            PatientHealthMetric bpMetric = new PatientHealthMetric();
            bpMetric.setPatient(patient);
            bpMetric.setDoctor(doctor);
            bpMetric.setMedicalRecord(medicalRecord);
            bpMetric.setRecordDate(metric.getRecordDate());
            bpMetric.setMeasurementDate(metric.getMeasurementDate());
            bpMetric.setMetricType("BLOOD_PRESSURE");
            bpMetric.setValue(String.valueOf(bloodPressure));
            bpMetric.setBloodPressure(bloodPressure);
            bpMetric.setNotes("Blood pressure measurement: " + bloodPressure + " mmHg");
            healthMetricRepository.save(bpMetric);
        }
        
        if (heartRate != null) {
        metric.setHeartRate(heartRate);
            
            // Also record as a typed metric
            PatientHealthMetric hrMetric = new PatientHealthMetric();
            hrMetric.setPatient(patient);
            hrMetric.setDoctor(doctor);
            hrMetric.setMedicalRecord(medicalRecord);
            hrMetric.setRecordDate(metric.getRecordDate());
            hrMetric.setMeasurementDate(metric.getMeasurementDate());
            hrMetric.setMetricType("HEART_RATE");
            hrMetric.setValue(String.valueOf(heartRate));
            hrMetric.setHeartRate(heartRate);
            hrMetric.setNotes("Heart rate measurement: " + heartRate + " bpm");
            healthMetricRepository.save(hrMetric);
        }
        
        metric.setNotes(notes);
        
        return healthMetricRepository.save(metric);
    }

    @Transactional(readOnly = true)
    public List<PatientHealthMetric> getAllHealthMetrics() {
        return healthMetricRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PatientHealthMetric> getHealthMetricById(Integer id) {
        return healthMetricRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PatientHealthMetric> getHealthMetricsByPatient(Patient patient) {
        return healthMetricRepository.findByPatientOrderByRecordDateAsc(patient);
    }

    /**
     * Get formatted chart data for a patient's health metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPatientHealthMetricsChartData(Patient patient) {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // Get all metrics for the patient
            List<PatientHealthMetric> allMetrics = getHealthMetricsByPatient(patient);
            
            if (allMetrics != null && !allMetrics.isEmpty()) {
                // Filter metrics by type
                List<PatientHealthMetric> weightMetrics = allMetrics.stream()
                    .filter(m -> "WEIGHT".equals(m.getMetricType()))
                    .sorted((m1, m2) -> m1.getMeasurementDate().compareTo(m2.getMeasurementDate()))
                    .collect(java.util.stream.Collectors.toList());
                
                List<PatientHealthMetric> bpMetrics = allMetrics.stream()
                    .filter(m -> "BLOOD_PRESSURE".equals(m.getMetricType()))
                    .sorted((m1, m2) -> m1.getMeasurementDate().compareTo(m2.getMeasurementDate()))
                    .collect(java.util.stream.Collectors.toList());
                
                List<PatientHealthMetric> hrMetrics = allMetrics.stream()
                    .filter(m -> "HEART_RATE".equals(m.getMetricType()))
                    .sorted((m1, m2) -> m1.getMeasurementDate().compareTo(m2.getMeasurementDate()))
                    .collect(java.util.stream.Collectors.toList());
                
                // Prepare weight data
                if (!weightMetrics.isEmpty()) {
                    String[] weightDates = new String[weightMetrics.size()];
                    Double[] weights = new Double[weightMetrics.size()];
                    
                    for (int i = 0; i < weightMetrics.size(); i++) {
                        PatientHealthMetric metric = weightMetrics.get(i);
                        weightDates[i] = metric.getMeasurementDate().toString();
                        
                        // Try to get weight from value field first, then from weight field
                        if (metric.getValue() != null && !metric.getValue().isEmpty()) {
                            try {
                                weights[i] = Double.parseDouble(metric.getValue());
                        } catch (NumberFormatException e) {
                                weights[i] = metric.getWeight(); // Fallback to weight field
                            }
                        } else {
                            weights[i] = metric.getWeight();
                        }
                    }
                    
                    chartData.put("weightDates", weightDates);
                    chartData.put("weights", weights);
                } else {
                    chartData.put("weightDates", new String[0]);
                    chartData.put("weights", new Double[0]);
                }
                
                // Prepare blood pressure data
                if (!bpMetrics.isEmpty()) {
                    String[] bpDates = new String[bpMetrics.size()];
                    String[] bloodPressures = new String[bpMetrics.size()];
                    
                    for (int i = 0; i < bpMetrics.size(); i++) {
                        PatientHealthMetric metric = bpMetrics.get(i);
                        bpDates[i] = metric.getMeasurementDate().toString();
                        
                        // Blood pressure is typically stored in the value field
                        if (metric.getValue() != null && !metric.getValue().isEmpty()) {
                            bloodPressures[i] = metric.getValue();
                        } else if (metric.getBloodPressure() != null) {
                            bloodPressures[i] = metric.getBloodPressure().toString();
                        } else {
                            bloodPressures[i] = "N/A";
                        }
                    }
                    
                    chartData.put("bpDates", bpDates);
                    chartData.put("bloodPressures", bloodPressures);
                } else {
                    chartData.put("bpDates", new String[0]);
                    chartData.put("bloodPressures", new String[0]);
                }
                
                // Prepare heart rate data
                if (!hrMetrics.isEmpty()) {
                    String[] hrDates = new String[hrMetrics.size()];
                    Integer[] heartRates = new Integer[hrMetrics.size()];
                    
                    for (int i = 0; i < hrMetrics.size(); i++) {
                        PatientHealthMetric metric = hrMetrics.get(i);
                        hrDates[i] = metric.getMeasurementDate().toString();
                        
                        // Try to get heart rate from value field first, then from heartRate field
                        if (metric.getValue() != null && !metric.getValue().isEmpty()) {
                            try {
                                heartRates[i] = Integer.parseInt(metric.getValue().trim());
                            } catch (NumberFormatException e) {
                                heartRates[i] = metric.getHeartRate(); // Fallback to heartRate field
                            }
                        } else {
                            heartRates[i] = metric.getHeartRate();
                        }
                    }
                    
                    chartData.put("hrDates", hrDates);
                    chartData.put("heartRates", heartRates);
                } else {
                    chartData.put("hrDates", new String[0]);
                    chartData.put("heartRates", new Integer[0]);
                }
                
                // For backward compatibility
                chartData.put("dates", allMetrics.stream()
                    .map(m -> m.getMeasurementDate().toString())
                    .toArray(String[]::new));
            } else {
                // Add empty data
                chartData.put("weightDates", new String[0]);
                chartData.put("weights", new Double[0]);
                chartData.put("bpDates", new String[0]);
                chartData.put("bloodPressures", new String[0]);
                chartData.put("hrDates", new String[0]);
                chartData.put("heartRates", new Integer[0]);
                chartData.put("dates", new String[0]);
            }
        } catch (Exception e) {
            logger.error("Error generating health metrics chart data", e);
            // Add empty data in case of error
            chartData.put("weightDates", new String[0]);
            chartData.put("weights", new Double[0]);
            chartData.put("bpDates", new String[0]);
            chartData.put("bloodPressures", new String[0]);
            chartData.put("hrDates", new String[0]);
            chartData.put("heartRates", new Integer[0]);
            chartData.put("dates", new String[0]);
        }
        
        return chartData;
    }

    /**
     * Generate a chart image for the patient's health metrics and return the URL path
     * 
     * @param patient the patient
     * @param metricType the metric type (WEIGHT, BLOOD_PRESSURE, HEART_RATE)
     * @param startDate start date for chart data
     * @param endDate end date for chart data
     * @return URL path to the generated chart image
     * @throws IOException if there's an error generating the chart
     */
    @Transactional(readOnly = true)
    public String generateMetricChart(Patient patient, String metricType, LocalDate startDate, LocalDate endDate) {
        try {
            logger.info("Generating chart for patient ID: {}, metric type: {}, date range: {} to {}", 
                patient.getId(), metricType, startDate, endDate);
                
            // Find health metrics of the given type within the date range
            List<PatientHealthMetric> metrics = healthMetricRepository.findByPatientAndMetricTypeAndMeasurementDateBetweenOrderByMeasurementDateAsc(
                    patient, metricType, startDate, endDate);
            
            if (metrics == null || metrics.isEmpty()) {
                logger.info("No metrics found with measurement date range, trying alternative queries");
                // If no metrics with measurement date range, try to use all metrics of this type
                // get all metrics for this patient of this type and filter manually
                metrics = healthMetricRepository.findByPatientAndMetricTypeOrderByMeasurementDateDesc(patient, metricType);
                
                // Filter by date range if measurement date is available
                metrics = metrics.stream()
                    .filter(m -> m.getMeasurementDate() != null)
                    .filter(m -> !m.getMeasurementDate().isBefore(startDate) && !m.getMeasurementDate().isAfter(endDate))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (metrics == null || metrics.isEmpty()) {
                logger.info("Still no metrics found, trying with record_date instead");
                // If still no metrics, try using record_date instead of measurement_date
                metrics = healthMetricRepository.findByPatient(patient);
                metrics = metrics.stream()
                    .filter(m -> metricType.equals(m.getMetricType()))
                    .filter(m -> !m.getRecordDate().isBefore(startDate) && !m.getRecordDate().isAfter(endDate))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // If we still have no data, try to fetch all metrics of this type regardless of date
            if (metrics == null || metrics.isEmpty()) {
                logger.info("Trying to fetch all metrics of type {} regardless of date", metricType);
                metrics = healthMetricRepository.findByPatientAndMetricTypeOrderByMeasurementDateDesc(patient, metricType);
                if (metrics == null || metrics.isEmpty()) {
                    logger.warn("No health metrics found for patient {} of type {}", patient.getId(), metricType);
                    return null; // No data available at all
                }
            }
            
            logger.info("Found {} metrics for chart generation", metrics.size());
            
            // Dump data for debugging
            for (PatientHealthMetric metric : metrics) {
                logger.debug("Metric data: type={}, value={}, date={}", 
                    metric.getMetricType(), metric.getValue(),
                    metric.getMeasurementDate() != null ? metric.getMeasurementDate() : metric.getRecordDate());
            }
            
            // Create dataset for the chart
            org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
            
            // Format for dates on chart - show month-day AND day of week for better readability
            java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MM-dd (EEE)");
            
            // Keep track of whether we've added any data points
            boolean dataAdded = false;
            
            // Sort metrics by date before adding to chart
            metrics.sort((m1, m2) -> {
                LocalDate d1 = m1.getMeasurementDate() != null ? m1.getMeasurementDate() : m1.getRecordDate();
                LocalDate d2 = m2.getMeasurementDate() != null ? m2.getMeasurementDate() : m2.getRecordDate();
                return d1.compareTo(d2);
            });
            
            // Add data to the dataset based on metric type
            for (PatientHealthMetric metric : metrics) {
                // Use measurement date if available, otherwise record date
                LocalDate dateToUse = metric.getMeasurementDate() != null ? metric.getMeasurementDate() : metric.getRecordDate();
                if (dateToUse != null) {
                    String dateStr = dateToUse.format(dateFormatter);
                    
                    try {
                        switch (metricType) {
                            case "WEIGHT":
                                // Try to get value from the value field first
                                if (metric.getValue() != null && !metric.getValue().isEmpty()) {
                                    try {
                                    double weight = Double.parseDouble(metric.getValue());
                                    dataset.addValue(weight, "Weight", dateStr);
                                        dataAdded = true;
                                        logger.debug("Added weight data point: {} on {}", weight, dateStr);
                                    } catch (NumberFormatException e) {
                                        logger.warn("Invalid weight value format: {}", metric.getValue());
                                    }
                                } 
                                // Fall back to the weight field if value is not set
                                else if (metric.getWeight() != null) {
                                    dataset.addValue(metric.getWeight(), "Weight", dateStr);
                                    dataAdded = true;
                                    logger.debug("Added weight data point from weight field: {} on {}", metric.getWeight(), dateStr);
                                }
                                break;
                                
                            case "BLOOD_PRESSURE":
                                // Try value field first - blood pressure is usually stored as "systolic/diastolic"
                                if (metric.getValue() != null && metric.getValue().contains("/")) {
                                    String[] parts = metric.getValue().split("/");
                                    if (parts.length == 2) {
                                        try {
                                        int systolic = Integer.parseInt(parts[0].trim());
                                        int diastolic = Integer.parseInt(parts[1].trim());
                                            
                                            // For blood pressure, we want to show both systolic and diastolic values
                                        dataset.addValue(systolic, "Systolic", dateStr);
                                        dataset.addValue(diastolic, "Diastolic", dateStr);
                                            dataAdded = true;
                                            
                                            logger.debug("Added blood pressure data points: {}/{} on {}", 
                                                systolic, diastolic, dateStr);
                                        } catch (NumberFormatException e) {
                                            logger.warn("Invalid blood pressure format: {}", metric.getValue());
                                        }
                                    }
                                }
                                // Fall back to bloodPressure field if available
                                else if (metric.getBloodPressure() != null) {
                                    dataset.addValue(metric.getBloodPressure(), "Blood Pressure", dateStr);
                                    dataAdded = true;
                                    logger.debug("Added blood pressure data point from field: {} on {}", 
                                        metric.getBloodPressure(), dateStr);
                                } 
                                // For blood pressure, also try the value field directly as a number
                                else if (metric.getValue() != null && !metric.getValue().isEmpty()) {
                                    try {
                                        int bp = Integer.parseInt(metric.getValue().trim());
                                        dataset.addValue(bp, "Blood Pressure", dateStr);
                                        dataAdded = true;
                                        logger.debug("Added blood pressure data point from value as number: {} on {}", 
                                            bp, dateStr);
                                    } catch (NumberFormatException e) {
                                        logger.warn("Could not parse blood pressure value: {}", metric.getValue());
                                    }
                                }
                                break;
                                
                            case "HEART_RATE":
                                // Try value field first
                                if (metric.getValue() != null && !metric.getValue().isEmpty()) {
                                    try {
                                        int heartRate = Integer.parseInt(metric.getValue().trim());
                                    dataset.addValue(heartRate, "Heart Rate", dateStr);
                                        dataAdded = true;
                                        logger.debug("Added heart rate data point: {} on {}", heartRate, dateStr);
                                    } catch (NumberFormatException e) {
                                        logger.warn("Invalid heart rate format: {}", metric.getValue());
                                    }
                                }
                                // Fall back to heartRate field
                                else if (metric.getHeartRate() != null) {
                                    dataset.addValue(metric.getHeartRate(), "Heart Rate", dateStr);
                                    dataAdded = true;
                                    logger.debug("Added heart rate data point from field: {} on {}", 
                                        metric.getHeartRate(), dateStr);
                                }
                                break;
                        }
                    } catch (Exception e) {
                        // Log the error but continue processing other metrics
                        logger.error("Error processing metric: {}", e.getMessage(), e);
                    }
                }
            }
            
            // If no valid data points added, return null
            if (!dataAdded || dataset.getRowCount() == 0) {
                logger.warn("No valid data points for chart of type {}", metricType);
                return null;
            }
            
            // Create chart with appropriate title and labels
            String chartTitle = getChartTitle(metricType);
            String yAxisLabel = getYAxisLabel(metricType);
            
            org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
                chartTitle,      // Chart title
                "Date",          // X-axis label
                yAxisLabel,      // Y-axis label
                dataset,         // Dataset
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,            // Include legend
                true,            // Include tooltips
                false            // Include URLs
            );
            
            // Add basic customization to make data points more visible
            try {
                // Set background color
                chart.setBackgroundPaint(java.awt.Color.white);
                
                // Get the plot and customize it
                org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
                plot.setBackgroundPaint(java.awt.Color.lightGray);
                plot.setDomainGridlinePaint(java.awt.Color.white);
                plot.setRangeGridlinePaint(java.awt.Color.white);
                
                // Customize the renderer to show data points clearly
                org.jfree.chart.renderer.category.LineAndShapeRenderer renderer = 
                    (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
                renderer.setDefaultShapesVisible(true);
                renderer.setDrawOutlines(true);
                renderer.setUseFillPaint(true);
                renderer.setDefaultFillPaint(java.awt.Color.white);
                
                // Make shapes larger so they're more visible
                renderer.setDefaultShape(new java.awt.geom.Ellipse2D.Double(-5, -5, 10, 10));
                
                // Customize weight chart display
                if ("WEIGHT".equals(metricType)) {
                    // Set Y-axis range to make weight data more visible
                    org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
                    
                    // Attempt to find a reasonable range for the data
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    
                    // Find min and max values
                    for (int row = 0; row < dataset.getRowCount(); row++) {
                        for (int col = 0; col < dataset.getColumnCount(); col++) {
                            Number value = dataset.getValue(row, col);
                            if (value != null) {
                                min = Math.min(min, value.doubleValue());
                                max = Math.max(max, value.doubleValue());
                            }
                        }
                    }
                    
                    // If we found data points, set a better range
                    if (min != Double.MAX_VALUE && max != Double.MIN_VALUE) {
                        // Add padding (10% of the range)
                        double padding = (max - min) * 0.1;
                        // Ensure at least some padding
                        padding = Math.max(padding, 2.0);
                        
                        // Set the range with padding
                        rangeAxis.setRange(Math.max(0, min - padding), max + padding);
                        
                        logger.info("Setting axis range for {} chart: min={}, max={}, with padding", 
                            metricType, min - padding, max + padding);
                    } else {
                        // Default sensible range for weight if no data found
                        rangeAxis.setRange(40, 100);
                        logger.warn("No data points found for range calculation, using default range");
                    }
                    
                    // Make the weight line blue
                    renderer.setSeriesPaint(0, java.awt.Color.BLUE);
                }
                
                // Customize blood pressure chart
                if ("BLOOD_PRESSURE".equals(metricType)) {
                    org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
                    
                    // Typical blood pressure ranges (add padding)
                    rangeAxis.setRange(40, 180);
                    
                    // Set colors for systolic (red) and diastolic (blue)
                    if (dataset.getRowCount() > 0) {
                        String series0 = (String) dataset.getRowKey(0);
                        if ("Systolic".equals(series0)) {
                            renderer.setSeriesPaint(0, java.awt.Color.RED);
                            
                            // If we have systolic, we likely have diastolic too
                            if (dataset.getRowCount() > 1) {
                                renderer.setSeriesPaint(1, java.awt.Color.BLUE);
                            }
                        } else {
                            // Single blood pressure value case
                            renderer.setSeriesPaint(0, java.awt.Color.RED);
                        }
                    }
                }
                
                // Customize heart rate chart
                if ("HEART_RATE".equals(metricType)) {
                    org.jfree.chart.axis.NumberAxis rangeAxis = (org.jfree.chart.axis.NumberAxis) plot.getRangeAxis();
                    
                    // Typical heart rate ranges (add padding)
                    rangeAxis.setRange(40, 120);
                    
                    // Set color for heart rate (green)
                    renderer.setSeriesPaint(0, java.awt.Color.GREEN.darker());
                }
                
                // Add a legend
                chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
                
                logger.info("Dataset summary for {}: rows={}, columns={}, total values={}", 
                    metricType, dataset.getRowCount(), dataset.getColumnCount(), 
                    dataset.getRowCount() * dataset.getColumnCount());
                
                // Log detailed dataset info for debugging
                StringBuilder dataPoints = new StringBuilder("Dataset values:\n");
                for (int row = 0; row < dataset.getRowCount(); row++) {
                    String series = (String) dataset.getRowKey(row);
                    dataPoints.append("Series: ").append(series).append("\n");
                    
                    for (int col = 0; col < dataset.getColumnCount(); col++) {
                        String category = (String) dataset.getColumnKey(col);
                        Number value = dataset.getValue(row, col);
                        if (value != null) {
                            dataPoints.append("  ").append(category).append(": ").append(value).append("\n");
                        }
                    }
                }
                logger.info(dataPoints.toString());
            } catch (Exception e) {
                logger.error("Error customizing chart: {}", e.getMessage(), e);
            }
            
            try {
                // Ensure the charts directory exists
            String chartsDirPath = "src/main/resources/static/images/charts/";
            java.io.File chartsDir = new java.io.File(chartsDirPath);
                logger.info("Attempting to use charts directory: {}", chartsDir.getAbsolutePath());
                
                // Try creating the directory if it doesn't exist
                if (!chartsDir.exists()) {
                    boolean created = chartsDir.mkdirs();
                    logger.info("Chart directory created: {}, exists now: {}", created, chartsDir.exists());
                } else {
                    logger.info("Chart directory already exists: {}", chartsDir.exists());
                }

                // Check if the directory is writable
                if (!chartsDir.canWrite()) {
                    logger.error("Chart directory is not writable: {}", chartsDir.getAbsolutePath());
                    
                    // Try alternative directories if needed
                    chartsDirPath = System.getProperty("java.io.tmpdir") + "/healthcare-charts/";
                    chartsDir = new java.io.File(chartsDirPath);
            if (!chartsDir.exists()) {
                        boolean created = chartsDir.mkdirs();
                        logger.info("Alternative chart directory created: {}", created);
                    }
                    logger.info("Using alternative chart directory: {}", chartsDir.getAbsolutePath());
            }
            
                // Generate unique filename with timestamp to avoid browser caching
            String filename = String.format("%s_%s_%d.png", 
                patient.getId(),
                metricType.toLowerCase(),
                System.currentTimeMillis()
            );
            
            // Save chart as PNG image
                java.io.File outputFile = new java.io.File(chartsDir, filename);
                logger.info("Writing chart to file: {}", outputFile.getAbsolutePath());
                
                try {
            org.jfree.chart.ChartUtils.saveChartAsPNG(outputFile, chart, 800, 400);
                    logger.info("Chart file successfully written: {}, exists: {}, size: {} bytes", 
                        outputFile.getAbsolutePath(), 
                        outputFile.exists(),
                        outputFile.exists() ? outputFile.length() : 0);
                    
                    if (!outputFile.exists() || outputFile.length() == 0) {
                        logger.error("Failed to create chart image or image is empty: {}", outputFile.getAbsolutePath());
                        return null;
                    }
                } catch (Exception e) {
                    logger.error("Error saving chart to file: {}", e.getMessage(), e);
                    return null;
                }
                
                // Return the web-accessible URL path with a timestamp cache-buster parameter
                return "/images/charts/" + filename + "?t=" + System.currentTimeMillis();
            } catch (Exception e) {
                logger.error("Error creating chart directory or file: {}", e.getMessage(), e);
                return null;
            }
        } catch (Exception e) {
            // Log the error and return null
            logger.error("Error generating metric chart: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get the appropriate chart title based on metric type
     */
    private String getChartTitle(String metricType) {
        switch (metricType) {
            case "WEIGHT": return "Weight History";
            case "BLOOD_PRESSURE": return "Blood Pressure History";
            case "HEART_RATE": return "Heart Rate History";
            default: return metricType + " History";
        }
    }

    /**
     * Get the appropriate Y-axis label based on metric type
     */
    private String getYAxisLabel(String metricType) {
        switch (metricType) {
            case "WEIGHT": return "Weight (kg)";
            case "BLOOD_PRESSURE": return "Blood Pressure (mmHg)";
            case "HEART_RATE": return "Heart Rate (bpm)";
            default: return "Value";
        }
    }

    /**
     * Gets the most recent health metrics for a patient, up to the specified limit
     */
    @Transactional(readOnly = true)
    public List<PatientHealthMetric> getRecentHealthMetricsByPatient(Patient patient, int limit) {
        List<PatientHealthMetric> allMetrics = healthMetricRepository.findByPatientOrderByRecordDateDesc(patient);
        
        // Return up to 'limit' metrics
        return allMetrics.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }
} 