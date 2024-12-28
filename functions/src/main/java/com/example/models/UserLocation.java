package com.example.appointment.models;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLocation {

    private String type;
    private double[] coordinates;
    private String locationName;

    private String address;


//    public UserLocation(double longitude, double latitude,String locationName, String address) {
//        this.type = "Point";
//        this.coordinates = new double[]{longitude, latitude};
//        this.locationName = locationName;
//        this.address = address;
//    }

}
