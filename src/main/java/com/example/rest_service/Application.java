package com.example.rest_service;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class Application extends ResourceConfig {

	public Application() {
		packages("com.example.controller");
		register(com.example.rest_service.exception.GlobalExceptionHandler.class);
	}
}
