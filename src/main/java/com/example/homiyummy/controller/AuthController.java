package com.example.homiyummy.controller;

import com.example.homiyummy.model.auth.ChangePassRequest;
import com.example.homiyummy.model.auth.EmailRequest;
import com.example.homiyummy.model.auth.LoginRequestDTO;
import com.example.homiyummy.model.auth.LoginResponseDTO;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.UserService;
import com.example.homiyummy.service.UserTypeService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final UserTypeService userTypeService;

    public  AuthController(
            UserService userService,
            AuthService authService,
            UserTypeService userTypeService) {
        this.userService = userService;
        this.authService = authService;
        this.userTypeService = userTypeService;
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping(value = "/login", produces = "application/json")
    public CompletableFuture<?> login(@RequestBody LoginRequestDTO request) {
        try {
            // SOLICITAMOS ACCESO EN AUTHENTICATION Y GUARDAMOS EL UID DEL USUARIO QUE NOS DEVUELVE
            String uid = authService.authenticateUser(request.getEmail(), request.getPassword());

            // OBTENEMOS EL TIPO DE USUARIO QUE ES
            return userTypeService.getUserTypeByUid(uid)
                    .thenApply(userType -> {
                        // Y LO DEVOLVEMOS AL FRONTEND JUNTO AL UID EN UN OBJETO LoginResponseDTO
                        LoginResponseDTO loginResponse = new LoginResponseDTO(uid, userType);
                        return ResponseEntity.ok(loginResponse);
                    }).exceptionally(e -> {
                        // SI SE PRODUCE UN ERROR DEVOLVEMOS EL OBJETO VACÍO
                        LoginResponseDTO errorResponse = new LoginResponseDTO("", "");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                    });
        } catch (Exception e) {
            // SI SE PRODUCE UN ERROR DEVOLVEMOS EL OBJETO VACÍO
            LoginResponseDTO errorResponse = new LoginResponseDTO("", "");
            CompletableFuture<ResponseEntity<LoginResponseDTO>> failedFuture = new CompletableFuture<>();
            failedFuture.complete(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
            return failedFuture;
        }
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/getUser")
    public CompletableFuture<ResponseEntity<String>> getUid(@RequestBody EmailRequest emailRequest) {

        return authService.getUidFromEmail(emailRequest.getEmail())                     // DEVOLVEMOS UNA PETICIÓN A getUidFromEmail QUE NOS TRAE UN FUTURO
                .thenApply(uid -> ResponseEntity.ok("{\"uid\": \"" + uid + "\"}"))// SI SALE BIEN, OBTENEMOS EL UID Y LO DEVOLVEMOS UN JSON CON EL UID Y SU VALOR.
                .exceptionally(e -> {                                                   // SI SALE MAL, DEVOLVEREMOS UN JSON EN UN STRING CON EL UID VACÍO.
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    return new ResponseEntity<>("{\"uid\": }", HttpStatus.NOT_FOUND);
                });
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/changePassword")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> changePassword(@RequestBody ChangePassRequest changePassRequest) throws FirebaseAuthException { // CLASE ChangePassRequest Q CREO EN PACKAGE auth PARA PODER PONER AQUÍ UN OBJETO DE ESTE TIPO

        // CREAMOS UN MAP EN EL QUE ENVIAREMOS LA RESPUESTA AL FRONT
        Map<String, Boolean> result = new HashMap<>();

        // SI NO VIENE UN PASSWORD ENVIAMOS UNA RESPUESTA "false"
        if (changePassRequest.getPassword() == null || changePassRequest.getPassword().isEmpty()) {
            result.put("change", false);
            return CompletableFuture.completedFuture(ResponseEntity.ok(result));
        }
        // SI VIENE UN PASSWORD TRAMITAMOS EL CAMBIO DE CONTRASEÑA
        return authService.changeUserPassword(changePassRequest.getUid(), changePassRequest.getPassword())
                .thenApply(ResponseEntity::ok)  // SI ES EXITOSO ENVIAMOS LA RESPUESTA AL FRONT
                .exceptionally(e -> {
                    result.put("change", false);// SI DA ERROR ENVIAMOS UN false AL FRONT
                    return ResponseEntity.ok(result);
                });
    }

// ------------------------------------------------------------------------------------------------------------


}






























