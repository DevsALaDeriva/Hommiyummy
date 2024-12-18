package com.example.homiyummy.service;

import com.example.homiyummy.repository.AuthRepository;
import com.example.homiyummy.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

    @Service
    public class AuthService {

        private UserRepository userRepository;
        private final DatabaseReference bbddRef;
        private final FirebaseAuth firebaseAuth;
        private final AuthRepository authRepository;

        public AuthService(FirebaseApp firebaseApp,
                           UserRepository userRepository,
                           FirebaseAuth firebaseAuth,
                           AuthRepository authRepository){
            bbddRef = FirebaseDatabase.getInstance().getReference();
            this.firebaseAuth = firebaseAuth;
            this.authRepository = authRepository;
            this.userRepository = userRepository;
        }


        /**
         * CREA UN USUARIO EN AUTHENTICATION (USER O RESTAURANT) Y DEVUELVE SU ID
         * PRIMERO LO GUARDAMOS EN AUTHENTICATION
         * SEGUNDO CON EL UID YA CREADO, LO MANDAMOS AL SERVICE Y REPOSITORIO PARA CREARLO EN REALTIME
         * @param email EMAIL DEL USUARIO A REGISTRAR
         * @param password PASSWORD DEL USUARIO A REGISTRAR
         * @return EL UID DEL USUARIO CREADO
         * @throws FirebaseAuthException
         */
        public String createUser(String email, String password) throws FirebaseAuthException {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);
            UserRecord userRecord = firebaseAuth.createUser(request);
            return userRecord.getUid();
        }


        /**
         *  HEMOS GUARDADO LAS VARIABLES DE ENTORNO, QUITÁNDOLAS DE application.properties PARA QUE NO DEN PROBLEMAS AL SUBIRLO A UN REPOSITORIO Y SEA SEGURO
         *  POR SI DIERAN ALGÚN PROBLEMA, PONEMOS UN IF EN CADA UNA
         * @param email DEL USUARIO
         * @param password DE USUARIO
         * @return
         * @throws Exception
         */
        public String authenticateUser(String email, String password) throws Exception {


            String firebaseAuthUrl = System.getenv("FIREBASE_AUTH_URL");
            if (firebaseAuthUrl == null) {
                throw new IllegalStateException("La variable de entorno FIREBASE_AUTH_URL no está configurada");
            }

            String firebaseApiKey = System.getenv("FIREBASE_API_KEY");
            if (firebaseApiKey == null) {
                throw new IllegalStateException("La variable de entorno FIREBASE_API_KEY no está configurada");
            }

            String url = firebaseAuthUrl + firebaseApiKey;  // CREAMOS UNA STRING COMPLETA SUMANDO AMBAS STRING

            RestTemplate restTemplate = new RestTemplate(); // RestTemplate ES LA HERRAMIENTA QUE TIENE EL BACKEND PARA ACTUAR CONMO UN CLIENTE HTTP Y PODER MANDAR LA SOLICITUD A Firebase Y RECIBIR UNA RESPUESTA
            HttpHeaders headers = new HttpHeaders();        // CREA LOS ENCABEZADOS HTTP PARA LA SOLICITUD (DECIMOS QUE EL Content-Type es application/json PARA DECIR QUE EL CUERPO DE LA SOLICITUD ES UN JSON)

            headers.set("Content-Type", "application/json");

            String requestBody = String.format(             // CREAMOS EL CUERPO DE LA SOCLITIUTD
                    "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                    email, password
            );
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return (String) response.getBody().get("localId");
            } else {
                throw new Exception("Authentication failed");
            }
        }



        public CompletableFuture<String> getUidFromEmail(String email) {
            return authRepository.getUidFromEmail(email);
        }



        public CompletableFuture<Map<String, Boolean>> changeUserPassword(String uid, String newPass) {

            CompletableFuture<Map<String, Boolean>> data = new CompletableFuture<>();

            try {
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setPassword(newPass);  // CREAMOS UNA PETICIÓN PARA CAMBIAR LA CONTRASEÑA
                UserRecord userRecord = FirebaseAuth.getInstance().updateUser(request);                     // ACTUALIZAMOS EL USUARIO CON LA NUEVA CONTRASEÑA (LO GUARDO EN VARIABLE POR SI LO NECESITO LUEGO)

                Map<String, Boolean> result = new HashMap<>();
                result.put("change", true);
                data.complete(result);

            } catch (FirebaseAuthException | IllegalArgumentException e) {
                Map<String, Boolean> errorResult = new HashMap<>();
                errorResult.put("change", false);
                data.complete(errorResult);
            }

            return data;
        }


}
