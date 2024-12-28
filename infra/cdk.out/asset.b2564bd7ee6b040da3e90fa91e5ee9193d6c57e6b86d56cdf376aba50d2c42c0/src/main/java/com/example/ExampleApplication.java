package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.repository")
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


}
