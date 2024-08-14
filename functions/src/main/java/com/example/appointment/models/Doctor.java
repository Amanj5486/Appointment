package com.example.appointment.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.List;
import java.util.Set;


@Data
@Builder
@Document("DoctorUser")
public class Doctor implements UserDetails {

    @Id
    private String id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @Indexed
    @JsonProperty("designation")
    private String designation;

    @JsonProperty("languages")
    @Indexed
    private List<String> languages;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("doctor_description")
    private String doctorDescription;

    @JsonProperty("dob")
    private Long dob;

    @JsonProperty("email")
    private String email;

    @Indexed
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("authorities")
    private Set<Roles> authorities;

    @JsonProperty("dosop")
    private Long dosop;

    @Indexed
    @JsonProperty("ug_education")
    private String ugEducation;

    @Indexed
    @JsonProperty("pg_education")
    private List<String> pgEducation;

    @Indexed
    @JsonProperty("doctor_symptoms")
    private List<String> doctorSymptoms;

    @JsonProperty("image_url")
    private String imageURL;

    @JsonProperty("timing")
    String timing;

    @JsonProperty("patient_rec")
    String patientRec;

    @JsonProperty("hospital_rec")
    String hospitalRec;

    @JsonProperty("fee")
    String fee;

    @JsonProperty("n_reviews")
    String nReviews;

    @JsonProperty("n_patients")
    String nPatients;

    @JsonProperty("locations")
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private  List<UserLocation> locations;


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
