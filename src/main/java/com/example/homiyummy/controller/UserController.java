package com.example.homiyummy.controller;

import com.example.homiyummy.model.user.UserDTO;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.UserService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController // RestController para que nos permita manejar peticiones entrantes
@RequestMapping("/client") // Es el path base
public class UserController {


    private final UserService userService;
    private final AuthService authService;
    //private final DatabaseReference databaseReference;

    public UserController(UserService userService,
                          AuthService authService
                          //,DatabaseReference databaseReference
    ) {
        this.authService = authService;
        this.userService = userService;
        //this.databaseReference = databaseReference;
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
                //CREO USUARIO EN AUTH Y DEVUELVE EL Id
             String uid = authService.createUser(userDTO.getEmail(), userDTO.getPassword());
             System.out.println("------>" + uid);
             userDTO.setUid(uid); // PONEMOS EL ID GENERADO AL UserDTO

                // AUNQUE DEVOLVEMOS UN STRING, QUIERO RECIBIR EL UserResponse
                // LO MANDO AL SERVICE DEL USER PARA QUE LO CREE EN REALTIME
            UserResponse userResponse = userService.createUser(userDTO);                              // COMO createUser EN EL SERVICIO DEVUELVE UN UserResponse ENTREGADO POR UN FUTURO, LA OPERACIÓN ES ASÍNCRONA Y NO DA ERROR AQUÍ

                // DE ESE UserResponse OBTENIDO, SÓLO DEVUELVO UN JSON CON EL ID
            return ResponseEntity.ok("{\"uid\": \"" + userResponse.getUid() + "\"}");            // DEVOLVEMOS EL ID AL FRONTEND

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": false }");  // DEVOLVEMOS false AL FRONTEND SI HAY UN ERROR
        }
    }
// ------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<Map<String, Boolean>> updateUser( @RequestBody UserDTO userDTO) {

        // Verifica si `allergens` es una cadena vacía y, si es así, la reemplaza con una lista vacía
        if (userDTO.getAllergens() != null && userDTO.getAllergens().size() == 1 && userDTO.getAllergens().get(0).isEmpty()) {
            userDTO.setAllergens(new ArrayList<>()); // Reemplaza con lista vacía
        }

        Boolean change = userService.updateUser(userDTO);
        Map<String, Boolean> response = new HashMap<>();
        response.put("change", change);
        return ResponseEntity.ok(response);
    }

// ------------------------------------------------------------------------------------------------------------
@PostMapping("/getByUID")
public UserReadResponse getClient(@RequestBody UserReadRequest userReadRequest){
        String uid = userReadRequest.getUid();
        return userService.findUserByUid(uid);
}

// ------------------------------------------------------------------------------------------------------------







}

