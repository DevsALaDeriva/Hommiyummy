package com.example.homiyummy.controller;

import com.example.homiyummy.model.user.UserDTO;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.UserService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController // RestController para que nos permita manejar peticiones entrantes
@RequestMapping("/client") // Es el path base
public class UserController {


    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService,
                          AuthService authService
    ) {
        this.authService = authService;
        this.userService = userService;
    }

// ------------------------------------------------------------------------------------------------------------
    /**
     *
     * @param userDTO recibo en un json todas las propiedades que tenddrá el usuario
     *  1º Crea el usuarioo en Authentication (con el email y password que traen las propiedades)
     *  2º Añade el uid generado para añadírselo al objeto userDTO antes de enviarlo al servicio
     *  3º Se lo pasa al métod createUser del Servicio. El resultado devolverá un UserResponse y lo guarda en userResponse.
     * @return Devuelve ese ResponseEntity.ok si sale bien con la id que trae el userResponse desde Realtime.
     *         Si sale mal, devuelve un ResponseEntity.badRequest
     */

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {

        try {
            if(userDTO.getEmail() == null || userDTO.getEmail().isEmpty()){
                return ResponseEntity.badRequest().body("{\"uid\": \"\"}");
            }

            String uid = authService.createUser(userDTO.getEmail(), userDTO.getPassword());
            userDTO.setUid(uid);
            UserResponse userResponse = userService.createUser(userDTO);                              // COMO createUser EN EL SERVICIO DEVUELVE UN UserResponse ENTREGADO POR UN FUTURO, LA OPERACIÓN ES ASÍNCRONA Y NO DA ERROR AQUÍ

            return ResponseEntity.ok("{\"uid\": \"" + userResponse.getUid() + "\"}");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": \"\"}");
        }
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public CompletableFuture<ResponseEntity<Map<String, Boolean>>> updateUser( @RequestBody UserDTO userDTO) {

        Map<String, Boolean> response = new HashMap<>();
        String uid = userDTO.getUid();

        if (uid == null || uid.isEmpty()) {
            response.put("change", false);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(response)); // SI NO HAY UID DEVUELE UN  404
        }

        return userService.existsByUid(uid)
                .thenCompose(exists -> {
            if(!exists) {
                response.put("change", false);
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)); // Devuelve 404 si no existe
            }
            else {
                if (userDTO.getAllergens() != null && userDTO.getAllergens().size() == 1 && userDTO.getAllergens().get(0).isEmpty()) {
                    userDTO.setAllergens(new ArrayList<>());
                }

                return userService.updateUser(userDTO)
                        .thenApply(success -> {
                            response.put("change", success);
                            if(success) {
                                return ResponseEntity.ok(response);
                            }
                            else {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                            }
                        });

            }
        });
    }

// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/getByUID")
    public CompletableFuture<UserReadResponse> getClient(@RequestBody UserReadRequest userReadRequest){
        String uid = userReadRequest.getUid();
        return userService.findUserByUid(uid).exceptionally(ex -> new UserReadResponse()); // SI HAY ALGUN ERROR ENVÍA AL FRONT UN OBJETO VACÍO
    }

// ------------------------------------------------------------------------------------------------------------





}

