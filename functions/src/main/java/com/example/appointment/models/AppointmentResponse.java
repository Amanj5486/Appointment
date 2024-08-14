package com.example.appointment.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentResponse {

    Doctor doctor;
    Appointments appointments;
    Double distance;

}
