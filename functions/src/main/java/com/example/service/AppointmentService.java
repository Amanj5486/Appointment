package com.example.service;

import com.example.exception.ApplicationException;
import com.example.models.*;
import com.example.repository.*;
import com.example.utils.DateUtils;
import com.example.utils.DistanceCalculatorUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GeoNearOperation;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.print.Doc;
import java.awt.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class AppointmentService {

    @Autowired
    AppointmentsRepository appointmentsRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    S3Service s3Service;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;



    public List<AppointmentResponse> getAppointment(String appointmentId, String patientId, String doctorId, LocalDate date,Integer appointmentStatus,String designation,List<String> doctorSymptoms,List<String>languages,
                                                    Double patientLat,Double patientLng, Boolean sort,List<String> disease){
        List<AppointmentResponse> response = new ArrayList<>();
        if(appointmentId != null){
            Optional<Appointments> optionalAppointments = appointmentsRepository.findById(appointmentId);
            if(optionalAppointments.isEmpty()){
                throw new ApplicationException(ApplicationError.APPOINTMENT_NOT_FOUND);
            }
            Appointments appointments = optionalAppointments.get();
            AppointmentResponse appointmentResponse = AppointmentResponse.builder().build();
            Doctor doctor = doctorRepository.findById(appointments.getDoctorId()).get();
            appointmentResponse.setDoctor(doctor);
            appointmentResponse.setAppointments(appointments);
            if(patientLat!=null && patientLng!=null)
                appointmentResponse.setDistance(DistanceCalculatorUtils.calculateDistance(patientLat,patientLng,appointments.getUserLocation().getCoordinates()[1],
                        appointments.getUserLocation().getCoordinates()[0]));
            response.add(appointmentResponse);
            return response;
        }
        Set<String> doctorIds = new HashSet<>();
        if(designation!=null && languages!=null){
            List<Doctor> doctors = doctorRepository.findAllByDesignationAndLanguages(designation,languages);
            doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toSet());
        }
        else if(designation!=null){
            List<Doctor> doctors = doctorRepository.findAllByDesignation(designation);
            doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toSet());
        }
        else if(languages!=null){
            List<Doctor> doctors = doctorRepository.findAllByLanguages(languages);
            doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toSet());
        }
        if(doctorSymptoms!=null && languages!=null){
            List<Doctor> doctors = doctorRepository.findAllByDoctorSymptomsAndLanguages(doctorSymptoms,languages);
            doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toSet());
        }
        else if(doctorSymptoms!=null){
            List<Doctor> doctors = doctorRepository.findAllByDoctorSymptoms(doctorSymptoms);
            doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toSet());
        }
        if(disease!=null){
            List<Doctor> doctors = doctorRepository.findAllByDoctorSymptoms(doctorSymptoms);
            doctorIds.addAll(doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toSet()));
        }
        if(doctorId!=null){
            doctorIds.add(doctorId);
        }
        List<String> doctors = doctorIds.stream().toList();
        List<Appointments> appointmentsList = new ArrayList<>();
        LocalTime time = LocalTime.parse("00:00:00",DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime startDateTime = LocalDateTime.of(date,time);
        time = LocalTime.parse("23:59:59",DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDateTime endDateTime = LocalDateTime.of(date,time);


        ZonedDateTime zonedDateTime = startDateTime.atZone(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime zonedEndDateTime = endDateTime.atZone(ZoneId.of("Asia/Kolkata"));
        Long epochStartDateTime = zonedDateTime.toEpochSecond();
        Long epochEndDateTime = zonedEndDateTime.toEpochSecond();


        if (sort && patientLng!=null && patientLat!=null) {
            double [] coordinates  = {patientLng,patientLat};
            appointmentsList = appointmentsRepository.findDoctorsByCriteria(coordinates,500000,patientId,doctors,appointmentStatus,epochStartDateTime,epochEndDateTime);
        }
        else{
           appointmentsList = appointmentsRepository.findAllByPatientIdAndDoctorIdInAndAppointmentStatusAndStartDateTimeBetween(patientId,doctors,appointmentStatus,epochStartDateTime,epochEndDateTime);
        }
        List<Doctor> doctorList = new ArrayList<>();

        Set<String> uniqueDoctorIds = appointmentsList.stream()
                .map(Appointments::getDoctorId)
                .collect(Collectors.toSet());

        doctorList = doctorRepository.findAllById(uniqueDoctorIds);

        Map<String, Doctor> doctorMap = new HashMap<>();
        doctorList.forEach(doctor -> {
            String imageUrl = s3Service.generatePresignedUrl("doctors/"+doctor.getId()+"/profile.jpeg","doctor-image-1");
            doctor.setImageURL(imageUrl);
            doctorMap.put(doctor.getId(), doctor);
        });

        appointmentsList.parallelStream().forEach(e ->{
            AppointmentResponse appointmentResponse = AppointmentResponse.builder().build();
            appointmentResponse.setDoctor(doctorMap.get(e.getDoctorId()));
            appointmentResponse.setAppointments(e);
            if(patientLat!=null && patientLng!=null)
            appointmentResponse.setDistance(DistanceCalculatorUtils.calculateDistance(patientLat,patientLng,e.getUserLocation().getCoordinates()[1],
                    e.getUserLocation().getCoordinates()[0]));
            response.add(appointmentResponse);

        });
        return response;
    }



    public void setAvailability(String doctorId, LocalDate date, Map<LocalTime,LocalTime> time, Integer incrementMinutes,int locationId){
        Doctor doctor = doctorRepository.findById(doctorId).get();
        UserLocation userLocation = doctor.getLocations().get(locationId);
        List<Appointments> appointmentsList = new ArrayList<>();
        Long startDateTimeEpoch;
        Long endDateTimeEpoch;
        for (Map.Entry<LocalTime,LocalTime> entry : time.entrySet()) {
            LocalTime  currentKey = entry.getKey();
            LocalTime endTime = entry.getValue();
            while (currentKey.isBefore(endTime)) {
                LocalDateTime dateTime = LocalDateTime.of(date,currentKey);
                LocalDateTime endDateTime = LocalDateTime.of(date,currentKey.plusMinutes(incrementMinutes));
                ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.of("Asia/Kolkata"));
                ZonedDateTime zonedEndDateTime = endDateTime.atZone(ZoneId.of("Asia/Kolkata"));
                startDateTimeEpoch = zonedDateTime.toEpochSecond();
                endDateTimeEpoch = zonedEndDateTime.toEpochSecond();
                Appointments appointments = Appointments.builder().doctorId(doctorId)
                        .appointmentStatus(0)
                        .userLocation(userLocation)
                        .startDateTime(startDateTimeEpoch)
                        .endDateTime(endDateTimeEpoch).build();
                appointmentsList.add(appointments);
                currentKey = currentKey.plusMinutes(incrementMinutes);
            }

        }
        appointmentsRepository.saveAll(appointmentsList);

    }

//    public Appointments addAppointment(Appointments appointment){
//        if(appointment.getDoctorId()==null){
//            throw new ApplicationException(400,"doctor id not found","doctor id not found");
//        }
//        if(doctorRepository.findById(appointment.getDoctorId()).isEmpty()){
//            throw new ApplicationException(400,"doctor id is invalid","doctor id is invalid");
//        }
//        if(appointment.getDate()==null){
//            throw new ApplicationException(400,"date not found","date not found");
//        }
//        if(appointment.getStartTime()==null || appointment.getEndTime()==null){
//            throw new ApplicationException(400,"start time/ end time is null","start time/ end time is null");
//        }
//        appointmentsRepository.save(appointment);
//        return appointment;
//    }

    public Appointments updateAppointment(Appointments appointment){
        if(appointment.getId()==null){
            throw new ApplicationException(400,"appointment id null","provide appointment id to update");
        }
        Optional<Appointments> optionalExistingAppointments =  appointmentsRepository.findById(appointment.getId());
        if (optionalExistingAppointments.isEmpty()) {
            throw new ApplicationException(400,"apointment not found","appointment not found");
        }

        Appointments existingAppointments = optionalExistingAppointments.get();
        Field[] fields = appointment.getClass().getDeclaredFields();
        boolean updated = false;
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object newValue = field.get(appointment);
                Object existingValue = field.get(existingAppointments);

                if (newValue != null) {
                    if (field.getType().isAssignableFrom(List.class)) {
                        List<Object> newList = (List<Object>) newValue;
                        List<Object> existingList = (List<Object>) existingValue;
                        if (existingList != null) {
                            existingList.addAll(newList);
                        } else {
                            field.set(existingAppointments, newList);
                        }
                        updated = true;
                    } else if (!newValue.equals(existingValue)) {
                        field.set(existingAppointments, newValue);
                        updated = true;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if(updated){
            return appointmentsRepository.save(existingAppointments);
        }
        else{
            throw new ApplicationException(400,"no changes/updates found","no changes/updates found");
        }
    }

}
