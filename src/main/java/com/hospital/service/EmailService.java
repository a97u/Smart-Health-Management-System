package com.hospital.service;

import com.hospital.entity.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for sending email notifications
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send appointment confirmation email
     */
    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String doctorName = appointment.getDoctor().getUser().getName();
            String date = appointment.getAppointmentDate().toString();
            
            sendAppointmentConfirmation(patientEmail, doctorName, date);
        } catch (Exception e) {
            logger.error("Error sending appointment confirmation email", e);
        }
    }

    /**
     * Send appointment confirmation email with detailed parameters
     */
    @Async
    public void sendAppointmentConfirmation(String patientEmail, String doctorName, String appointmentDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patientEmail);
            message.setSubject("Appointment Confirmation");
            message.setText("Your appointment with Dr. " + doctorName + 
                    " has been scheduled for " + appointmentDate + 
                    ".\n\nThank you for choosing our hospital.");
            
            mailSender.send(message);
            logger.info("Appointment confirmation email sent to: " + patientEmail);
        } catch (Exception e) {
            logger.error("Error sending appointment confirmation email", e);
        }
    }

    /**
     * Send appointment cancellation email
     */
    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String doctorName = appointment.getDoctor().getUser().getName();
            String date = appointment.getAppointmentDate().toString();
            
            sendAppointmentCancellationNotification(patientEmail, doctorName, date);
        } catch (Exception e) {
            logger.error("Error sending appointment cancellation email", e);
        }
    }

    /**
     * Send appointment cancellation notification with detailed parameters
     */
    @Async
    public void sendAppointmentCancellationNotification(String patientEmail, String doctorName, String appointmentDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patientEmail);
            message.setSubject("Appointment Cancellation");
            message.setText("Your appointment with Dr. " + doctorName + 
                    " scheduled for " + appointmentDate + 
                    " has been cancelled.\n\nPlease contact us if you need to reschedule.");
            
            mailSender.send(message);
            logger.info("Appointment cancellation email sent to: " + patientEmail);
        } catch (Exception e) {
            logger.error("Error sending appointment cancellation email", e);
        }
    }

    /**
     * Send appointment reminder email
     */
    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String doctorName = appointment.getDoctor().getUser().getName();
            String date = appointment.getAppointmentDate().toString();
            
            sendAppointmentReminderNotification(patientEmail, doctorName, date);
        } catch (Exception e) {
            logger.error("Error sending appointment reminder email", e);
        }
    }
    
    /**
     * Send appointment reminder notification with detailed parameters
     */
    @Async
    public void sendAppointmentReminderNotification(String patientEmail, String doctorName, String appointmentDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patientEmail);
            message.setSubject("Appointment Reminder");
            message.setText("This is a reminder that you have an appointment with Dr. " + doctorName + 
                    " tomorrow, " + appointmentDate + 
                    ".\n\nPlease arrive 15 minutes early to complete any necessary paperwork." +
                    "\n\nIf you need to reschedule, please contact us as soon as possible.");
            
            mailSender.send(message);
            logger.info("Appointment reminder email sent to: " + patientEmail);
        } catch (Exception e) {
            logger.error("Error sending appointment reminder email", e);
        }
    }

    /**
     * Send appointment update notification with detailed parameters
     */
    @Async
    public void sendAppointmentUpdatedNotification(String patientEmail, String doctorName, String appointmentDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patientEmail);
            message.setSubject("Appointment Update");
            message.setText("Your appointment with Dr. " + doctorName + 
                    " has been rescheduled to " + appointmentDate + 
                    ".\n\nPlease contact us if this new time does not work for you.");
            
            mailSender.send(message);
            logger.info("Appointment update email sent to: " + patientEmail);
        } catch (Exception e) {
            logger.error("Error sending appointment update email", e);
        }
    }

    /**
     * Send generic notification email
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            logger.info("Email sent to: " + to);
        } catch (Exception e) {
            logger.error("Error sending email", e);
        }
    }
}