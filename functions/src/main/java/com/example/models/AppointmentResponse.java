package com.example.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class AppointmentResponse {

    Doctor doctor;
    Appointments appointments;
    Double distance;

}
