package com.example.appointment.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Data
@Builder
public class Prescription {

    String patientName;

    String doctorName;

    String age;

    String sex;

    String bp;

    String temp;

    String oxygenLevel;

    String  date;
    String sugarLevel;

    List<String> allergies;

    List<String> patientSymptoms;

    List<String> doctorSymptoms;

    List<String> labTest;


    List<String> medicines;

    String followups;

    Boolean chatEnabled;

    String otherComment;

    Boolean typed;

    String uri;

    String weight;

    Integer appointmentId;


    public String convertDTOtoBase64(Prescription prescription) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert DTO to JSON string
            String jsonString = objectMapper.writeValueAsString(prescription);

            // Encode JSON string to Base64
            String base64String = Base64.getEncoder().encodeToString(jsonString.getBytes());

            return base64String;
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Handle exception appropriately
            return null;
        }
    }

    public void decodeBase64AndSetFields(String base64Data) {
//        base64Data = "eyJhcmdzIjp7ImRldmljZV9uYW1lIjoiTWljcm9pbnZlcnRlciAoU05vLiAxMjIzMzgwMDAwMjQpIiwic25vIjoiMTIyMzM4MDAwMDI0IiwiZXZlbnRfaWRlbnRpZmllciI6bnVsbH0sImRldmljZV9uYW1lIjoiTWljcm9pbnZlcnRlciAoU05vLiAxMjIzMzgwMDAwMjQpIiwibXNnX3BhcmFtcyI6Im51bGwiLCJvcGVuX21pY3Jvc19jb3VudCI6MCwidHlwZSI6Ik1pY3JvaW52ZXJ0ZXIiLCJldmVudF9rZXkiOm51bGx9";
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);


        try {

            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            // Parse JSON and populate fields
            String modifiedjson = json.replace("'", "\"");
//            log.info("modification json:",modifiedjson);
            Prescription decodedDto = new ObjectMapper().readValue(modifiedjson, Prescription.class);

            // Copy fields to this instance
            this.patientName = decodedDto.getPatientName();
            this.age = decodedDto.getAge();
            this.sex = decodedDto.getSex();
            this.bp = decodedDto.getBp();
            this.temp = decodedDto.getTemp();
            this.oxygenLevel = decodedDto.getOxygenLevel();
            this.date = decodedDto.getDate();
            this.sugarLevel = decodedDto.getSugarLevel();
            this.allergies = decodedDto.getAllergies();
            this.patientSymptoms = decodedDto.getPatientSymptoms();
            this.doctorSymptoms =decodedDto.getDoctorSymptoms();
            this.labTest = decodedDto.getLabTest();
            this.medicines = decodedDto.getMedicines();
            this.followups = decodedDto.getFollowups();
            this.chatEnabled = decodedDto.getChatEnabled();
            this.otherComment = decodedDto.getOtherComment();
            this.typed = decodedDto.getTyped();
            this.uri = decodedDto.getUri();
            this.doctorName = decodedDto.getDoctorName();
            this.weight = decodedDto.getWeight();
            this.appointmentId= decodedDto.getAppointmentId();

        } catch (IOException e) {
            // Handle parsing exception
            e.printStackTrace(); // Handle exception appropriately
        }
    }


}
