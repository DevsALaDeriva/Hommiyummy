package com.example.homiyummy.database.config;

import com.example.homiyummy.database.FirebaseInitializer;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


@Configuration
public class FirebaseAuthConfig {
    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        FirebaseInitializer.initializeFirebase(); // NOS ASEGURAMOS DE Q FIREBASE ESTÁ INICIALIZADO
        return FirebaseAuth.getInstance();        // OBTENGO INSTANCIA DE FirebaseAuth QUE USAREMOS PARA CREAR Y VERIFICAR USUARIOS
    }

}
