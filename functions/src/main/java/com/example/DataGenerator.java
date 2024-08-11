//package com.example;
//
//import com.example.models.Appointments;
//import com.example.models.Doctor;
//import com.example.models.Patient;
//import com.example.models.Roles;
//import com.example.repository.AppointmentsRepository;
//import com.example.repository.DoctorRepository;
//import com.example.service.AppointmentService;
//import com.github.javafaker.Faker;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Component
//public class DataGenerator implements CommandLineRunner {
//
//    @Autowired
//    DoctorRepository doctorRepository;
//
//    @Autowired
//    AppointmentService appointmentService;
//
//    @Autowired
//    AppointmentsRepository appointmentsRepository;
//
//    private static final Faker faker = new Faker();
//
//
//    @Override
//    public void run(String... args) throws Exception {
//
////        List<Appointments> appointmentsList = appointmentsRepository.findAll();
////        for(Appointments appointments:appointmentsList){
////           appointments.setDate(null);
////           appointments.setEndTime(null);
////           appointments.setStartTime(null);
////        }
////        appointmentsRepository.saveAll(appointmentsList);
////        System.out.println("done");
//
//    }
//
//    private static final List<String> SPECIALIZATIONS = Arrays.asList(
//            "Cardiologist", "Dermatologist", "Neurologist", "Oncologist", "Orthopedic Surgeon",
//            "Pediatrician", "Psychiatrist", "Radiologist", "General Practitioner", "Endocrinologist",
//            "Gastroenterologist", "Hematologist", "Nephrologist", "Pulmonologist", "Rheumatologist",
//            "Urologist", "Gynecologist", "Ophthalmologist", "Anesthesiologist", "ENT Specialist"
//    );
//
//
//
//}
