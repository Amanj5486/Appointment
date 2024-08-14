package com.example.appointment.service;

import com.example.appointment.models.Doctor;
import com.example.appointment.models.Patient;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {


    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Doctor> doctor = doctorRepository.findByUsername(username);
        UserDetails user = null;
        if(doctor.isPresent()){
            user = doctor.get();
        }
        else{
            Optional<Patient> patient = patientRepository.findByUsername(username);
            if(patient.isPresent()){
                user = patient.get();
            }
        }
     if(user==null){
         throw  new UsernameNotFoundException("doctor/patient is not valid");
     }
     else{
         return  user;
     }

    }
}
