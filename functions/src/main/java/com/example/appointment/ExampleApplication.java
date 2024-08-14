package com.example.appointment;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.beans.factory.annotation.Value;


@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.appointment.repository")
public class ExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleApplication.class, args);
	}


//	@Bean
//	public HandlerMapping handlerMapping() {
//		return new RequestMappingHandlerMapping();
//	}
//	@Bean
//	public HandlerAdapter handlerAdapter() {
//		return new RequestMappingHandlerAdapter();
//	}
//	@Bean
//	public MongoTemplate primaryMongoTemplate(){
//		MongoProperties properties =  new MongoProperties();
//		MongoClient mongoClient = MongoClients.create(properties.getUri());
//		return new MongoTemplate( mongoClient, properties.getDatabase());
//	}

	@Bean
	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

}
