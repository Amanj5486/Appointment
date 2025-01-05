package com.example.service;

import com.example.exception.ApplicationException;
import com.example.models.*;
import com.example.repository.AppointmentsRepository;
import com.example.repository.DoctorRepository;
import com.example.repository.PatientRepository;
import com.example.utils.DistanceCalculatorUtils;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.data.geo.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;


import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Autowired
    private MongoTemplate mongoTemplate;



    public List<AppointmentResponse> getAppointment(String appointmentId, String patientId, String doctorId, LocalDate startDate, LocalDate endDate, List<Integer> appointmentStatus, String designation, List<String> doctorSymptoms, List<String> languages, Double patientLat, Double patientLng, Boolean sort, List<String> disease) {
        List<AppointmentResponse> response = new ArrayList<>();

        // Handle appointment by ID
        if (appointmentId != null) {
            Optional<Appointments> optionalAppointments = appointmentsRepository.findById(appointmentId);
            if (optionalAppointments.isEmpty()) {
                throw new ApplicationException(ApplicationError.APPOINTMENT_NOT_FOUND);
            }

            Appointments appointments = optionalAppointments.get();
            Doctor doctor = doctorRepository.findById(appointments.getDoctorId()).orElseThrow(() -> new ApplicationException(ApplicationError.APPOINTMENT_NOT_FOUND));
            String imageUrl = s3Service.generatePresignedUrl("doctors/" + doctor.getId() + "/profile.jpg", "doctor-image-1");
            doctor.setImageURL(imageUrl);

            AppointmentResponse appointmentResponse = AppointmentResponse.builder()
                    .doctor(doctor)
                    .appointments(appointments)
                    .distance(calculateDistance(patientLat, patientLng, appointments))
                    .build();

            response.add(appointmentResponse);
            return response;
        }

        // Find matching doctors based on criteria
        Set<String> doctorIds = findDoctorsByCriteria(designation, doctorSymptoms, languages, disease, doctorId);

        // Build dynamic query
        Query query = new Query();
        if (patientId != null) {
            query.addCriteria(Criteria.where("patientId").is(patientId));
        }
        if (!doctorIds.isEmpty()) {
            query.addCriteria(Criteria.where("doctorId").in(doctorIds));
        }
        if (appointmentStatus != null && !appointmentStatus.isEmpty()) {
            query.addCriteria(Criteria.where("appointmentStatus").in(appointmentStatus));
        }

        // Handle date range
        addDateRangeCriteria(query, startDate, endDate);

        // Handle geo-location sorting
        if (sort && patientLng != null && patientLat != null) {
            Point location = new Point(patientLng, patientLat);
            query.addCriteria(Criteria.where("locations").near(location).maxDistance(500000));
        }

        List<Appointments> appointmentsList = mongoTemplate.find(query, Appointments.class);
        return buildAppointmentResponse(appointmentsList, patientLat, patientLng);
    }

    private Set<String> findDoctorsByCriteria(String designation, List<String> doctorSymptoms, List<String> languages, List<String> disease, String doctorId) {
        Set<String> doctorIds = new HashSet<>();

        if (designation != null && languages != null) {
            doctorIds.addAll(findDoctorIdsByDesignationAndLanguages(designation, languages));
        } else if (designation != null) {
            doctorIds.addAll(findDoctorIdsByDesignation(designation));
        } else if (languages != null) {
            doctorIds.addAll(findDoctorIdsByLanguages(languages));
        }

        if (doctorSymptoms != null && languages != null) {
            doctorIds.addAll(findDoctorIdsBySymptomsAndLanguages(doctorSymptoms, languages));
        } else if (doctorSymptoms != null) {
            doctorIds.addAll(findDoctorIdsBySymptoms(doctorSymptoms));
        }

        if (disease != null) {
            doctorIds.addAll(findDoctorIdsBySymptoms(disease));
        }

        if (doctorId != null) {
            doctorIds.add(doctorId);
        }

        return doctorIds;
    }

    private void addDateRangeCriteria(Query query, LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            LocalTime startTime = LocalTime.parse("00:00:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
            LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);

            if (endDate == null) {
                endDate = startDate;
            }

            LocalTime endTime = LocalTime.parse("23:59:59", DateTimeFormatter.ofPattern("HH:mm:ss"));
            LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

            ZonedDateTime zonedStartDateTime = startDateTime.atZone(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime zonedEndDateTime = endDateTime.atZone(ZoneId.of("Asia/Kolkata"));

            Long epochStartDateTime = zonedStartDateTime.toEpochSecond();
            Long epochEndDateTime = zonedEndDateTime.toEpochSecond();

            query.addCriteria(Criteria.where("startDateTime").gte(epochStartDateTime).lte(epochEndDateTime));
        }
    }

    private List<AppointmentResponse> buildAppointmentResponse(List<Appointments> appointmentsList, Double patientLat, Double patientLng) {
        List<AppointmentResponse> response = new ArrayList<>();

        Set<String> uniqueDoctorIds = appointmentsList.stream()
                .map(Appointments::getDoctorId)
                .collect(Collectors.toSet());

        Set<String> uniquePatientIds = appointmentsList.stream()
                .map(Appointments::getPatientId)
                .collect(Collectors.toSet());

        List<Doctor> doctorList = doctorRepository.findAllById(uniqueDoctorIds);
        Map<String, Doctor> doctorMap = doctorList.stream().collect(Collectors.toMap(Doctor::getId, doctor -> {
            String imageUrl = s3Service.generatePresignedUrl("doctors/" + doctor.getId() + "/profile.jpg", "doctor-image-1");
            doctor.setImageURL(imageUrl);
            return doctor;
        }));

        List<Patient> patientList = patientRepository.findAllById(uniquePatientIds);
        Map<String, Patient> patientMap = patientList.stream().collect(Collectors.toMap(Patient::getId, patient -> {
            String imageUrl = s3Service.generatePresignedUrl("patients/" + patient.getId() + "/profile.jpg", "patient-image-1");
            patient.setImageURL(imageUrl);
            return patient;
        }));

        appointmentsList.forEach(appointment -> {
            AppointmentResponse appointmentResponse = AppointmentResponse.builder()
                    .doctor(doctorMap.get(appointment.getDoctorId()))
                    .appointments(appointment)
                    .distance(calculateDistance(patientLat, patientLng, appointment))
                    .patient(patientMap.get(appointment.getPatientId()))
                    .build();

            response.add(appointmentResponse);
        });
        return response;
    }

    private Double calculateDistance(Double patientLat, Double patientLng, Appointments appointment) {
        if (patientLat != null && patientLng != null && appointment.getUserLocation() != null) {
            return DistanceCalculatorUtils.calculateDistance(patientLat, patientLng,
                    appointment.getUserLocation().getCoordinates()[1],
                    appointment.getUserLocation().getCoordinates()[0]);
        }
        return null;
    }

    // Methods to retrieve doctors by criteria
    private List<String> findDoctorIdsByDesignationAndLanguages(String designation, List<String> languages) {
        return doctorRepository.findAllByDesignationAndLanguages(designation, languages)
                .stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());
    }

    private List<String> findDoctorIdsByDesignation(String designation) {
        return doctorRepository.findAllByDesignation(designation)
                .stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());
    }

    private List<String> findDoctorIdsByLanguages(List<String> languages) {
        return doctorRepository.findAllByLanguages(languages)
                .stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());
    }

    private List<String> findDoctorIdsBySymptomsAndLanguages(List<String> doctorSymptoms, List<String> languages) {
        return doctorRepository.findAllByDoctorSymptomsAndLanguages(doctorSymptoms, languages)
                .stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());
    }

    private List<String> findDoctorIdsBySymptoms(List<String> doctorSymptoms) {
        return doctorRepository.findAllByDoctorSymptoms(doctorSymptoms)
                .stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());
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


//    public AppointmentResponse createDoctorAndAppointment(String patId,String name,Long dateTime,Long endDateTime,String designation,double lat,double lng,String locationName,String address){
//        String []names = name.split(" ");
//        double[] coordinates = new double[2];
//        coordinates[0] =lng;
//        coordinates[1] =lat;
//        Doctor doctor = Doctor.builder()
//                .firstName(names[0])
//                .lastName(names.length>1?names[1]:"")
//                .designation(designation)
//                .locations(List.of(UserLocation.builder().coordinates(coordinates).locationName(locationName).address(address).type("point").build())).build();
//        doctor = doctorRepository.save(doctor);
//
//        String imageUrl = s3Service.generatePresignedUrl("doctors/" + doctor.getId() + "/profile.jpg", "doctor-image-1");
//
//
//        Appointments appointments = Appointments.builder()
//                                    .doctorId(doctor.getId())
//                                    .patientId(patId)
//                                    .startDateTime(dateTime)
//                                    .endDateTime(endDateTime)
//                                    .appointmentStatus(2)
//                                    .typed(false).prescription(Prescription.builder().url().build()).build();
//
//
//
//
//
//    }

}
