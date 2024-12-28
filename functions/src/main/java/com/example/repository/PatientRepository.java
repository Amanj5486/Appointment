package com.example.appointment.repository;

import com.example.appointment.models.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient,String> {
    Optional<Patient> findByUsername(String username);

}
