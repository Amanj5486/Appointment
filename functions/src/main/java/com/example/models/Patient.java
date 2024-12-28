package com.example.appointment.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Document("PatientUser")
public class Patient implements UserDetails {

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("first_name")
    String firstName;

    @JsonProperty("last_name")
    String lastName;

    @Indexed
    @JsonProperty("username")
    String username;

    @JsonProperty("email")
    String email;

    @JsonProperty("sex")
    String sex;

    @JsonProperty("height")
    String height;

    @JsonProperty("weight")
    String weight;


    @JsonProperty("allergies")
    List<String> allergies;


    @JsonProperty("authorities")
    private Set<Roles> authorities;

    @JsonProperty("image_url")
    private String imageURL;

    @Indexed
    @JsonProperty("password")
    String password;

    @JsonProperty("locations")
    @Indexed
    private  List<UserLocation> locations;

    @JsonProperty("dob")
    private Long dob;

    @Override
    public String getUsername() {
        return username;
    }

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

}
