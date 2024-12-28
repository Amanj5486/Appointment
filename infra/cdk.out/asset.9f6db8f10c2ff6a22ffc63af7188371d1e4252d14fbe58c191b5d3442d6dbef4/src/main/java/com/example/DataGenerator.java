//package com.example;
//
//import com.example.models.*;
//import com.example.repository.AppointmentsRepository;
//import com.example.repository.DoctorRepository;
//import com.example.repository.PatientRepository;
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
//    @Autowired
//    PatientRepository patientRepository;
//
//    private static final Faker faker = new Faker();
//
//
//
////    @Override
////    public void run(String... args) throws Exception {
////     System.out.println(System.currentTimeMillis());
////        List<Appointments> appointmentsList = appointmentsRepository.findAll();
////
////        List<Map<String, String>> medications = new ArrayList<>();
////        List<Map<String, String>> test = new ArrayList<>();
////
////        for(Appointments appointments:appointmentsList){
////          appointments.setPatientId("6698f9ab8dfa0d537bcec5d8");
////          appointments.setAppointmentStatus(2);
////          UserLocation userLocation = appointments.getUserLocation();
////          userLocation.setLocationNumber("9305732748");
////          appointments.setUserLocation(userLocation);
////          Map<String,String> medicine = new HashMap<>();
////          medications = new ArrayList<>();
////          medicine.put("name",faker.medical().medicineName());
////          medicine.put("dosage","500mg");
////          medicine.put("frequency","1-1-1");
////          medicine.put("duration","2");
////          medications.add(medicine);
////            medicine.put("name",faker.medical().medicineName());
////            medicine.put("frequency","1-0-1");
////            medicine.put("duration","4");
////            medications.add(medicine);
////            medicine.put("name",faker.medical().medicineName());
////            medicine.put("frequency","0-0-1");
////            medicine.put("duration","30");
////            medications.add(medicine);
////            Map<String,String> test1 = new HashMap<>();
////            test = new ArrayList<>();
////            test1.put("name","Blood");
////            test1.put("conclusion","low WBC ");
////            test1.put("link","not available");
////            test.add(test1);
////            System.out.println(test.size());
////            System.out.println(medications.size());
////
////
////            Prescription prescription = Prescription.builder()
////                  .bp("110/150")
////                  .oxy("98")
////                  .sugar("120/200")
////                  .advice(List.of("Please avoid spicy food","Please drink 3-4L of water"))
////                  .followupDate(System.currentTimeMillis())
////                    .medications(medications)
////                    .diagnosis(List.of(faker.medical().diseaseName(),faker.medical().diseaseName()))
////                    .test(test)
////                  .build();
////            appointments.setPrescription(prescription);
////
////        }
////        appointmentsRepository.saveAll(appointmentsList);
////        System.out.println("done");
////
////        List<Patient> patientList = patientRepository.findAll();
////        for(Patient pat: patientList){
////            pat.setDob(798489000000L);
////        }
////        patientRepository.saveAll(patientList);
////    }
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
