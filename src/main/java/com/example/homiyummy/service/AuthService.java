package com.example.homiyummy.service;

import com.example.homiyummy.model.user.UserDTO;
import com.example.homiyummy.model.user.UserEntity;
import com.example.homiyummy.model.user.UserResponse;
import com.example.homiyummy.repository.AuthRepository;
import com.example.homiyummy.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private final Environment environment; // Es una interfaz de Spring que proporciona acceso a variables de entorno y propiedades configuradas en la aplicación, como las de application.properties o application.yml. Puedes usarla para obtener valores de configuración, como URLs, claves de API, configuraciones de bases de datos, etc.
                                           // Permite acceder a valores de configuración en cualquier método de la clase, generalmente a través de environment.getProperty("clave.de.propiedad").
                                           // Es útil cuando quieres acceder a diferentes propiedades en varios lugares del código, o cuando prefieres no inyectar cada propiedad individualmente con @Value.

    // CONSTRUCTOR PARA INYECTAR FIREBASEAPP
    public AuthService(FirebaseApp firebaseApp,
                       UserRepository userRepository,
                       FirebaseAuth firebaseAuth,
                       Environment environment,
                       AuthRepository authRepository){
        bbddRef = FirebaseDatabase.getInstance().getReference(); // INYECTA LA REFERENCIA DE LA BBDD
        this.firebaseAuth = firebaseAuth;
        this.environment = environment;
        this.authRepository = authRepository;
        this.userRepository = userRepository;

    }

    // ----------------------------------------------------------------------------------------------------------------

    // CREA UN USUARIO EN AUTHENTICATION (USER O RESTAURANT) Y DEVUELVE SU ID
    public String createUser(String email, String password) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest() // LO GUARDO EN AUTHENTICATION
                .setEmail(email)
                .setPassword(password);
        UserRecord userRecord = firebaseAuth.createUser(request);
        return userRecord.getUid();
    }

    // ----------------------------------------------------------------------------------------------------------------

    // PARTE DEL LOGIN
    public String authenticateUser(String email, String password) throws Exception {
        //String firebaseAuthUrl = environment.getProperty("firebase.auth.url"); // ACCEDEMOS CON LA INTERFAZ ENVIROMENT A DONDE ESTÁ GUARDADA LA URL en applications.properties
        //String firebaseApiKey = environment.getProperty("firebase.api.key");   //  ACCEDEMOS CON LA INTERFAZ ENVIROMENT A DONDE ESTÁ GUARDADA LA API-KEY en applications.properties

        // HEMOS GUARDADO LAS VARIABLES DE ENTORNO, QUITÁNDOLAS DE application.properties PARA QUE NO DEN PROBLEMAS AL SUBIRLO A UN REPOSITORIO Y SEA SEGURO
        // POR SI DIERAN ALGÚN PROBLEMA, PONEMOS UN IF EN CADA UNA
        String firebaseAuthUrl = System.getenv("FIREBASE_AUTH_URL");
        if (firebaseAuthUrl == null) {
            throw new IllegalStateException("La variable de entorno FIREBASE_AUTH_URL no está configurada");
        }

        String firebaseApiKey = System.getenv("FIREBASE_API_KEY");
        if (firebaseApiKey == null) {
            throw new IllegalStateException("La variable de entorno FIREBASE_API_KEY no está configurada");
        }

        String url = firebaseAuthUrl + firebaseApiKey;                         // CREAMOS UNA STRING COMPLETA SUMANDO AMBAS STRING

        RestTemplate restTemplate = new RestTemplate(); // RestTemplate ES LA HERRAMIENTA QUE TIENE EL BACKEND PARA ACTUAR CONMO UN CLIENTE HTTP Y PODER MANDAR LA SOLICITUD A Firebase Y RECIBIR UNA RESPUESTA
        HttpHeaders headers = new HttpHeaders();        // CREA LOS ENCABEZADOS HTTP PARA LA SOLICITUD. DECIMOS QUE EL Content-Type es application/json PARA DECIR QUE EL CUERPO DE LA SOLICITUD ES UN JSON

        headers.set("Content-Type", "application/json");

        String requestBody = String.format(             // CREAMOS EL CUERPO DE LA SOCLITIUTD
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
        );
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class); // Map.class INDICA EL TIPO DE RESPUESTA QUE SE ESPERA (UN CONJUNTO clave-valor)
        if (response.getStatusCode().is2xxSuccessful()) {
            return (String) response.getBody().get("localId"); // NOS TRAE EL UID DEL USUARIO
        } else {
            throw new Exception("Authentication failed");
        }
    }
    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<String> getUidFromEmail(String email) {
        return authRepository.getUidFromEmail(email);
    }

    // ----------------------------------------------------------------------------------------------------------------

//    public Boolean changeUserPassword(String uid, String newPass) {
//
//        try {
//            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setPassword(newPass);
//            UserRecord userRecord = FirebaseAuth.getInstance().updateUser(request);
//            System.out.println("Contraseña actualizada para el usuario: " + userRecord.getUid());
//            return true;
//        } catch (FirebaseAuthException e) {
//            System.err.println("Error al actualizar la contraseña: " + e.getMessage());
//            return false;
//        }
//
//    }

    public CompletableFuture<Map<String, Boolean>> changeUserPassword(String uid, String newPass) {

        CompletableFuture<Map<String, Boolean>> data = new CompletableFuture<>();

        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setPassword(newPass);  // CREAMOS UNA PETICIÓN PARA CAMBIAR LA CONTRASEÑA
            UserRecord userRecord = FirebaseAuth.getInstance().updateUser(request);                     // ACTUALIZAMOS EL USUARIO CON LA NUEVA CONTRASEÑA
            System.out.println("Contraseña actualizada para el usuario: " + userRecord.getUid());

            Map<String, Boolean> result = new HashMap<>();  // MAP EN EL QUE METEREMOS EL RESULTADO QUE ESPERA EL FRONTEND
            result.put("change", true);                     // FORMATO Q ESPERA RECIBIR EL FRONTEND
            data.complete(result);                          // ENVIAMOS LA CONFIRMACIÓN AL FRONTEND

        } catch (FirebaseAuthException | IllegalArgumentException e) {
            Map<String, Boolean> errorResult = new HashMap<>();
            errorResult.put("change", false); // RESPUESTA AL FRONTEND SI LA CONTRASEÑA NO CUMPLE
            data.complete(errorResult);

            System.err.println("Error al actualizar la contraseña: " + e.getMessage());
        }

        return data;
    }

































//    //--------------------------------------------------------------------------------
//    // COMO TIENE QUE VER CON AUTHENTICACION LO PONGO AQUÍ
//    // ESTE NO SÉ SI ES MEJOR AQUÍ O EN UserService
//    public static UserDTO getUserDtoFromToken(String token) throws FirebaseAuthException {
//        String userToken = token.replace("Bearer ", "");
//
//        // SI EL TOKEN ES CORRECTO NOS DA EL uid
//        String uid = FirebaseAuth.getInstance().verifyIdToken(userToken).getUid();
//
//        // OBTENEMOS EL USUARIO EN Firebase Auth DESDE SU UID ( NO OBTENEMOS LOS DE REALTIME, PARA ESO HAY QUE MONTAR EL POLLO DE LOS CALLBACKS)
//        UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
//
//        // CREAMMOS UN UserDTO
//        UserDTO userDTO = new UserDTO(userRecord.getUid());   // ID desde el UID de Firebase
//        userDTO.setUid(userRecord.getUid()); // ID desde el UID de Firebase
//        userDTO.setEmail(userRecord.getEmail() != null ? userRecord.getEmail() : "");
//        userDTO.setPhone(userRecord.getPhoneNumber() != null ? userRecord.getPhoneNumber() : "");
//        userDTO.setName((userRecord.getDisplayName() != null) ? userRecord.getDisplayName() : "");
//        // COMO NO SE GUARDA EN REALTIME, NI QUEREMOS, LA PONEMOS VACÍA
//        userDTO.setPassword("");
//
//        return userDTO;
//    }
//
//    public static String getUserIdFromToken(String token) throws FirebaseAuthException { // GUARDADO AQUÍ POR RECOMENDACIÓN DE CHATGPT
//        String userToken = token.replace("Bearer ", "");
//        return FirebaseAuth.getInstance().verifyIdToken(userToken).getUid(); // verifyIdToken COMPRUEBA QUE EL TOKEN ES CORRECTO. DEVUELVE UN FIREBASE TOKEN Y SOBRE ÉL OBTENGO EL UID
//    }







}
