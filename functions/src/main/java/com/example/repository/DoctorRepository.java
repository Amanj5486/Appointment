package com.example.repository;

import com.example.models.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends MongoRepository<Doctor,String> {

    Optional<Doctor> findById(String id);

    Optional<Doctor> findByUsername(String username);

    List<Doctor> findAllByDesignation(String designation);

    List<Doctor> findAllByDoctorSymptoms(List<String> doctorSymptoms);

    List<Doctor> findAllByLanguages(List<String>languages);

    List<Doctor> findAllByDesignationAndLanguages(String designation,List<String>languages);

    List<Doctor> findAllByDoctorSymptomsAndLanguages(List<String> doctorSymptoms,List<String>languages);

}
