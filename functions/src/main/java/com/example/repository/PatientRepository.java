package com.example.repository;

import com.example.models.Doctor;
import com.example.models.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient,String> {
    Optional<Patient> findByUsername(String username);

}
