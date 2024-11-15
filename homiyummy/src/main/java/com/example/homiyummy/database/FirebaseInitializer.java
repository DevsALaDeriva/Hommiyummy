package com.example.homiyummy.database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FirebaseInitializer {
    public static FirebaseApp initializeFirebase() throws IOException {
        // Obtén las variables de entorno
        String serviceAccountKey = System.getenv("FIREBASE_SERVICE_ACCOUNT_KEY");
        String databaseUrl = System.getenv("FIREBASE_DATABASE_URL");

        // Verifica que las variables de entorno estén configuradas
        if (serviceAccountKey == null || databaseUrl == null) {
            throw new IllegalStateException("Las variables de entorno FIREBASE_SERVICE_ACCOUNT_KEY o FIREBASE_DATABASE_URL no están configuradas");
        }

        // Convierte la clave de servicio JSON en un InputStream
        InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountKey.getBytes(StandardCharsets.UTF_8));

        // Configura FirebaseOptions usando las variables de entorno
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setDatabaseUrl(databaseUrl)
                .build();

        // Inicializa Firebase solo si aún no está inicializado
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        // Devuelve la instancia de FirebaseApp
        return FirebaseApp.getInstance();
    }
}






// USADA HASTA EL 10 NOVIEMBRE
//public class FirebaseInitializer {
//    public static FirebaseApp initializeFirebase() throws IOException {
//        FileInputStream serviceAccount = new FileInputStream("src/main/resources/homiyummy-b1d55-firebase-adminsdk-kr8za-41e28a46e0.json");
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .setDatabaseUrl("https://homiyummy-b1d55-default-rtdb.europe-west1.firebasedatabase.app/")
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            FirebaseApp.initializeApp(options); // Inicializa Firebase si no está ya configurado
//        }
//
//        return FirebaseApp.getInstance(); // Devuelve la instancia de FirebaseApp
//    }
//}

