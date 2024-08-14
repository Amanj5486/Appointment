package com.example.appointment.models;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document("Appointments")
@CompoundIndexes({
        @CompoundIndex(name = "unique_doctor_date_startDateTime",  def = "{'doctorId' : 1, 'startDateTime' : -1}", unique = true)
})
public class Appointments {


    @Id
    private String id;

    @Indexed
    @JsonProperty("doctor_id")
    private String doctorId;

    @Indexed
    @JsonProperty("patient_id")
    private  String patientId;   // -1 no active patient

    @Indexed
    @JsonProperty("start_date_time")
    private Long startDateTime;


    @JsonProperty("end_date_time")
    private Long endDateTime;

    @Indexed
    @JsonProperty("appoint_status")
    private Integer appointmentStatus; // 0 means appointment sent by patient and recieved by doctor  1 means appointment approved, -1

    @JsonProperty("patient_symptoms")
    private List<String> patientSymptoms;
    
    @JsonProperty("typed")
    Boolean typed;

    @JsonProperty("prescription")
    String prescription;

    @JsonProperty("user_location")
    UserLocation userLocation;

}
