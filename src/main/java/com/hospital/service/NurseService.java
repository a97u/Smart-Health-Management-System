package com.hospital.service;

import com.hospital.entity.Nurse;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.NurseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NurseService {

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Nurse registerNurse(Nurse nurse, User user) {
        User savedUser = userService.registerUser(user, Role.RoleName.NURSE);
        nurse.setUser(savedUser);
        return nurseRepository.save(nurse);
    }

    @Transactional(readOnly = true)
    public List<Nurse> getAllNurses() {
        return nurseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Nurse> getNurseById(Integer id) {
        return nurseRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Nurse> getNurseByUser(User user) {
        return nurseRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Nurse> getNurseByEmail(String email) {
        return nurseRepository.findByUserEmail(email);
    }

    @Transactional
    public Nurse updateNurse(Nurse nurse) {
        return nurseRepository.save(nurse);
    }

    @Transactional
    public void deleteNurse(Integer id) {
        nurseRepository.deleteById(id);
    }
}