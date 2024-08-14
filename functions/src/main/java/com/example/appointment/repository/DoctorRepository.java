package com.example.appointment.repository;

import com.example.appointment.models.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

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
