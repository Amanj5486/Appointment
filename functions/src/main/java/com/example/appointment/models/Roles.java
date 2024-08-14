package com.example.appointment.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;



@Document(value = "Roles")
public class Roles implements GrantedAuthority {

    @Id
    private String id;


    @Indexed
    private String authority;



    public Roles() {
        super();
    }



    public Roles(String authority) {
        this.authority = authority;
    }


    public Roles(String id, String authority) {
        this.id = id;
        this.authority = authority;
    }



    @Override
    public String getAuthority() {
        return this.authority;
    }

    public String getId() {
        return id;
    }

    public void setId(String roleID) {
        this.id = roleID;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
