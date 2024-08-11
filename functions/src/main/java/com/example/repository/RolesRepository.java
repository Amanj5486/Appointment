package com.example.repository;

import com.example.models.Roles;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RolesRepository extends MongoRepository<Roles,String> {
    Optional<Roles> findByAuthority(String authority);

}
