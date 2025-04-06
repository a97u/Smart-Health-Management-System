package com.hospital.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hospital.entity.Patient;
import com.hospital.entity.User;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
	Optional<Patient> findByUser(User user);

	Optional<Patient> findByUserEmail(String email);

	List<Patient> findByBloodGroup(String bloodGroup);
	
	Optional<Patient> findByPhoneNumber(String phoneNumber);
}
