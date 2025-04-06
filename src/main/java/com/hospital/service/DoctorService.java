package com.hospital.service;

import com.hospital.entity.Doctor;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Doctor registerDoctor(Doctor doctor, User user) {
        User savedUser = userService.registerUser(user, Role.RoleName.DOCTOR);
        doctor.setUser(savedUser);
        return doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(Integer id) {
        return doctorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorByUser(User user) {
        return doctorRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorByEmail(String email) {
        return doctorRepository.findByUserEmail(email);
    }

    @Transactional
    public Doctor updateDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        doctorRepository.deleteById(id);
    }
}