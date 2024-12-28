package com.example.appointment.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@PropertySource({ "classpath:application.properties" })
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    @Override
    protected String getDatabaseName() {
        return "Hospiconnect"; // Make sure to replace "your-database-name" with the actual name of your database
    }

    @Primary
    @Bean
    @Override
    public MongoClient mongoClient() {
        System.out.println("Aman "+ mongoUri);
        return MongoClients.create(mongoUri);
    }

    @Primary
    @Bean
    public MongoTemplate mongoTemplate(){
        return new MongoTemplate( mongoClient(), "Hospiconnect");
    }
}
