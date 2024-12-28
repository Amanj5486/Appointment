package com.example.appointment.repository;

import com.example.appointment.models.Roles;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface RolesRepository extends MongoRepository<Roles,String> {
    Optional<Roles> findByAuthority(String authority);

}
