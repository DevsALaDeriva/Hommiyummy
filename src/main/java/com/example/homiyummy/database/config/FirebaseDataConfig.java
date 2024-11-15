package com.example.homiyummy.database.config;

import com.example.homiyummy.database.FirebaseInitializer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FirebaseDataConfig {

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        return FirebaseInitializer.initializeFirebase();
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp);  // Obtiene la instancia de FirebaseDatabase utilizando FirebaseApp
    }

    @Bean
    public DatabaseReference databaseReference(FirebaseDatabase firebaseDatabase) {
        return firebaseDatabase.getReference(); // Define el bean DatabaseReference
    }


}
