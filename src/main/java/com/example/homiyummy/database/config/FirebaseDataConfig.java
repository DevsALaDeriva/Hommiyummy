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
        return FirebaseDatabase.getInstance(firebaseApp);  // Proporciona una instancia de FirebaseDatabase, que es el punto de entrada general para trabajar con Firebase Realtime Database en tu proyecto. Se usa para realizar configuraciones adicionales o acceder a distintas referencias dentro de la base de datos.
    }

    @Bean
    public DatabaseReference databaseReference(FirebaseDatabase firebaseDatabase) {
        return firebaseDatabase.getReference();            // Proporciona una referencia específica dentro de Firebase Realtime Database (normalmente la raíz, en este caso). Simplifica el acceso directo a los nodos de la base de datos.
    }


}
