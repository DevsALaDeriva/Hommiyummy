package com.example.homiyummy.controller;

import com.example.homiyummy.model.auth.ChangePassRequest;
import com.example.homiyummy.model.auth.EmailRequest;
import com.example.homiyummy.model.auth.LoginRequestDTO;
import com.example.homiyummy.model.auth.LoginResponseDTO;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.UserService;
import com.example.homiyummy.service.UserTypeService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController // RestController para que nos permita manejar peticiones entrantes
@RequestMapping("/auth") // Es el path base
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
            String uid = authService.authenticateUser(request.getEmail(), request.getPassword());

            return userTypeService.getUserTypeByUid(uid) // DEVOLVEMOS EL RESULTADO DEL MÉTOD getUserTYpe QUE CREAMOS EN EL SERVICIO NUEVO COMUN A USER Y RESTAURANTE
                    .thenApply(userType -> {
                        LoginResponseDTO loginResponse = new LoginResponseDTO(uid, userType);
                        return ResponseEntity.ok(loginResponse);
                    }).exceptionally(e -> {
                        LoginResponseDTO errorResponse = new LoginResponseDTO("", "");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                    });
        } catch (Exception e) {
            LoginResponseDTO errorResponse = new LoginResponseDTO("", "");
            CompletableFuture<ResponseEntity<LoginResponseDTO>> failedFuture = new CompletableFuture<>();
            failedFuture.complete(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
            System.out.println("---------- ENTRA ------- ");
            return failedFuture;
        }
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/getUser")
    public CompletableFuture<ResponseEntity<String>> getUid(@RequestBody EmailRequest emailRequest) {
        return authService.getUidFromEmail(emailRequest.getEmail())
                .thenApply(uid -> ResponseEntity.ok("{\"uid\": \"" + uid + "\"}"))
                .exceptionally(e -> {
                    throw new RuntimeException(e);
                });
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/changePassword")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> changePassword(@RequestBody ChangePassRequest changePassRequest) throws FirebaseAuthException { // CLASE ChangePassRequest CREADA EN PACKAGE auth PARA PODER PONER AQUÍ UN OBJETO DE ESTE TIPO

        Map<String, Boolean> result = new HashMap<>();

        if (changePassRequest.getPassword() == null || changePassRequest.getPassword().isEmpty()) { // PARA PREPARAR EL FUTURO ERROR TENEMOS QUE VALIDAR LA CONTRASEÑA
            result.put("change", false);                                                            // Y SI NO ES VÁLIDA NI SIQUIERA LA MANDAMOS AL SERVICIO Y CONTESTAMOS YA AL FRONTEND
            return CompletableFuture.completedFuture(ResponseEntity.ok(result));
        }

        return authService.changeUserPassword(changePassRequest.getUid(), changePassRequest.getPassword())// SI ES VÁLIDA, SE LA PASA AL SERVICIO
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> {
                    result.put("change", false); // Completa con "change": false en caso de cualquier error
                    return ResponseEntity.ok(result);
                });
    }

// ------------------------------------------------------------------------------------------------------------




}






























