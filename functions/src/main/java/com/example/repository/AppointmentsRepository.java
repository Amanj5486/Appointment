package com.example.repository;

import com.example.models.Appointments;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentsRepository extends MongoRepository<Appointments,String> {

    @Query("{ 'patientId': ?0, 'doctorId': { $in: ?1 }, 'appointmentStatus': ?2, 'startDateTime': { $gte: ?3, $lte: ?4 } }")
    List<Appointments> findAllByPatientIdAndDoctorIdInAndAppointmentStatusAndStartDateTimeBetween(String patientID, List<String>doctorIds, Integer appointmentStatus, Long startDateTime,Long endDateTime);


    @Query("{ 'locations': { $near: { $geometry: { type: 'Point', coordinates: ?0 }, $maxDistance: ?1 } }, " +
            "'appointments.patientId': ?2, 'appointments.doctorId': { $in: ?3 }, 'appointments.status': ?4, " +
            "'appointments.startDateTime': { $gte: ?5, $lte: ?6 } }")
    List<Appointments> findDoctorsByCriteria(double[] coordinates, double maxDistance, String patientId, List<String> doctorIds, Integer appointmentStatus, Long startDate, Long endDateTime);

}
