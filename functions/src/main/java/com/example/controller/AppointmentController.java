package com.example.controller;


import com.example.models.AppointmentResponse;
import com.example.models.Appointments;
import com.example.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Slf4j
@RequestMapping(value = "/appointment",produces = MediaType.APPLICATION_JSON_VALUE,consumes = APPLICATION_JSON_VALUE)
public class AppointmentController {


    @Autowired
    AppointmentService appointmentService;
    @GetMapping
    public List<AppointmentResponse> getAppointment(@RequestParam(value = "appointment_id",required = false) String appointmentId,
                                                    @RequestParam(value = "patient_id",required = false)String patientId,
                                                    @RequestParam(value = "doctor_id",required = false)String doctorId,
                                                    @RequestParam(value = "date",required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                                    @RequestParam(value = "appoint_status",required = false) Integer appointmentStatus,
                                                    @RequestParam(value = "designation",required = false) String designation,
                                                    @RequestParam(value = "doctor_symptom",required = false)List<String> doctorSymptoms,
                                                    @RequestParam(value = "languages",required = false)List<String> languages,
    @RequestParam(value="patient_lat",required = false) Double patientLat, @RequestParam(value="patient_lng",required = false) Double patientLng,
                                                    @RequestParam(value = "sort",required = false,defaultValue = "false") Boolean sort,
                                                    @RequestParam(value = "disease",required = false)List<String> disease
                                                    ){

        return appointmentService.getAppointment(appointmentId,patientId,doctorId,date,appointmentStatus,designation,doctorSymptoms,languages,patientLat,patientLng,sort,disease);
    }

    @PostMapping
    public Appointments updateAppointment(Appointments appointments){

        return appointmentService.updateAppointment(appointments);
    }

    @PutMapping
    public String setAvailability(@RequestParam(value = "doctor_id") String doctorId, @RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") String date,
                                  @RequestParam(value = "time") String timeRange, @RequestParam(value = "increment_minutes")Integer incrementMinutes,@RequestParam(value = "location_id",required = false)Integer locationId){
        try {
            LocalDate date1= LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<LocalTime, LocalTime> time = parseTimeRange(timeRange);
            appointmentService.setAvailability(doctorId,date1,time,incrementMinutes,locationId);
            return "saved";
        }
        catch (Exception e){
            return "unable to save "+ e.toString();
        }
    }






    private Map<LocalTime, LocalTime> parseTimeRange(String timeRange) {
        Map<LocalTime, LocalTime> timeMap = new HashMap<>();

        // Split the string by commas to get individual time pairs
        String[] timePairs = timeRange.split(",");

        for (String timePair : timePairs) {
            // Split each time pair by "=" to get start time and end time
            String[] pair = timePair.split("=");

            // Trim potential leading/trailing whitespaces
            String startTimeStr = pair[0].trim()+":00";
            String endTimeStr = pair[1].trim()+":00";

            // Assuming valid pair format, e.g., "08:00=09:00"
            System.out.println(startTimeStr);

            LocalTime startTime = LocalTime.parse(startTimeStr,DateTimeFormatter.ofPattern("HH:mm:ss"));
            LocalTime endTime = LocalTime.parse(endTimeStr,DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Add the pair to the map
            timeMap.put(startTime, endTime);
        }

        return timeMap;
    }

}
