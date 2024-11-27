package com.example.project_management_tool;  // Remplace avec le package correspondant à ton projet

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // C'est cette annotation qui indique le point d'entrée de l'application Spring Boot
public class ProjectManagementToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectManagementToolApplication.class, args);  // Lance l'application
	}
}
