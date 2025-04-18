package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.entity.Doctor;
import com.hospital.entity.User;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer>{
	Optional<Doctor> findByUser(User user);
	Optional<Doctor> findByUserEmail(String email);
}
