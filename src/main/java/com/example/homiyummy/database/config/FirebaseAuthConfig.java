package com.example.homiyummy.database.config;

import com.example.homiyummy.database.FirebaseInitializer;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


// SIRVE PARA INICIAR LA BASE DE DATOS Y PARA OBTENER UNA INSTANCIA DE AUTH

/**
 * 2. Clase FirebaseAuthConfiguration
 * Es una clase de configuración de Spring Boot que asegura que FirebaseAuth esté disponible como un Bean en el contenedor de Spring, lo que significa que puede ser inyectado automáticamente en otros lugares de la aplicación.
 * El método firebaseAuth primero llama a FirebaseInitializer.initializeFirebase() para asegurarse de que Firebase está inicializado antes de devolver una instancia de FirebaseAuth.
 * Luego, FirebaseAuth.getInstance() se utiliza para obtener una instancia de la clase FirebaseAuth, que maneja todas las operaciones de autenticación, como crear usuarios o verificar tokens.
 *
 * Contexto general:
 * Esta clase asegura que el servicio de autenticación de Firebase (FirebaseAuth) esté siempre accesible y preparado para ser usado en otras partes del código.
 *
 */


@Configuration
public class FirebaseAuthConfig {
    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        FirebaseInitializer.initializeFirebase(); // NOS ASEGURAMOS DE Q FIREBASE ESTÁ INICIALIZADO
        return FirebaseAuth.getInstance();        // OBTENGO INSTANCIA DE FirebaseAuth
    }
}
