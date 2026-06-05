package com.example.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentResponse {

    Doctor doctor;
    Appointments appointments;
    Double distance;
    Patient patient;
    String audioUploadUrl;
    String prescriptionUploadUrl;

}
