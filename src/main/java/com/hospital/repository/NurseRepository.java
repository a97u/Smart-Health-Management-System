package com.hospital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.entity.Nurse;
import com.hospital.entity.User;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Integer> {
	Optional<Nurse> findByUser(User user);

	Optional<Nurse> findByUserEmail(String email);
}
