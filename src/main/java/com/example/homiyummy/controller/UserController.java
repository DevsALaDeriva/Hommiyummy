package com.example.homiyummy.controller;

import com.example.homiyummy.model.order.OrderGetClientOrdersResponse;
import com.example.homiyummy.model.user.UserDTO;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.model.user.UserResponse;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.OrderService;
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

@RestController
@RequestMapping("/client")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final OrderService orderService;

    public UserController(UserService userService,
                          AuthService authService,
                          OrderService orderService
    ) {
        this.authService = authService;
        this.userService = userService;
        this.orderService = orderService;
    }

// ------------------------------------------------------------------------------------------------------------
    /**
     *
     * @param userDTO ES UN JSON QUE COVERTIMOS EN CLASE UserDTO CON LAS PROPIEDADES QUE TIENE EL USUARIO ENTRANTE
     *  1º CREAMOS AL USUARIO EN Authentication
     *  2º AÑADIMOS EL UID GENERADO AL OBJETO UserDTO
     *  3º Y LO MANDAMOS AL SERVICIO
     *  4º GUARDAMOS EN UN OBJETO UserResponse LO QUE EL SERVICIO NOS DEVUELVE
     * @return SI SALE BIEN DEVUELVE UN ResponseEntity.ok CON EL UID DE UserResponse.
     *         SI SALE MAL, DEVUELVE UN ResponseEntity.badRequest, EL FRONTEND RECIBE UN JSON CON EL UID VACÍO.
     */

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {

        try {
            if(userDTO.getEmail() == null || userDTO.getEmail().isEmpty()){
                return ResponseEntity.badRequest().body("{\"uid\": \"\"}");
            }

            String uid = authService.createUser(userDTO.getEmail(), userDTO.getPassword());
            userDTO.setUid(uid);
            UserResponse userResponse = userService.createUser(userDTO);

            return ResponseEntity.ok("{\"uid\": \"" + userResponse.getUid() + "\"}");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": \"\"}");
        }
    }

// ------------------------------------------------------------------------------------------------------------

    /**
     * OBTIENE EL UID DEL OBJETO ENTRANTE, SE ASEGURA QUE NI ES NULL NI ESTÁ VACÍO.
     * HACE UNA LLAMADA AL SERVICE PARA VER SI EXISTE.
     * SI EXISTE LO VUELVE A MANDAR AL SERVICE PARA ACTUALIZAR LOS DATOS.
     * @param userDTO OBJETO OBTENIDO DEL JSON QUE ENVÍA EL FRONTEND
     * @return PARA DEVOLVERLO EN UN JSON EMPLEA UN OBJETO  ResponseEntity QUE CONTIENE UN BOOLEAN.
     */
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

    /**
     * RECIBE UNA PETINCIÓN DEL FRONTEND, EXTRAE EL UID DEL USUARIO Y SE LO PASA AL SERVICE
     * @param userReadRequest OBJETO CON EL UID DEL USUARIO COMO ÚNICA PROPIEDAD
     * @return DEVUELVE UN UserReadResponse CON DATOS DENTRO DE UN CompletableFuture, O VACÍO SI DIO CUALQUIER PROBLEMA
     */
    @PostMapping("/getByUID")
    public CompletableFuture<UserReadResponse> getClient(@RequestBody UserReadRequest userReadRequest){
        String uid = userReadRequest.getUid();
        return userService.findUserByUid(uid).exceptionally(ex -> new UserReadResponse()); // SI HAY ALGUN ERROR ENVÍA AL FRONT UN OBJETO VACÍO
    }

// ------------------------------------------------------------------------------------------------------------

    /**
     * ENVÍA AL SERVICE EL UID DEL USUARIO.
     * COMO EL FRONTEND ESPERA UNA KEY LLAMADA "orders" CREAMOS UN MAP CON ELLA, Y COMO VALOR LE DAMOS UN ARRAY DE TIPO OrderGetClientOrdersResponse
     * QUE CONTIENE TODOS LOS PEDIDOS DE DICHO USUARIO
     * @param userRequest OBJETO FORMADO CON EL JSON ENVIADO POR EL FRONT END. SOLO CONTIENE EL UID DEL USUARIO
     * @return DEVUELVE UN ResponseEntity CON EL MAP, DENTRO DE UN CompletableFuture
     */
    @PostMapping("getOrders")
    public CompletableFuture<ResponseEntity<Map<String, ArrayList<OrderGetClientOrdersResponse>>>> getClientOrders(@RequestBody UserReadRequest userRequest){
        return orderService.getClientOrders(userRequest.getUid())
                .thenApply(ordersResponse -> {
                    Map<String, ArrayList<OrderGetClientOrdersResponse>> data = new HashMap<>();
                    data.put("orders", ordersResponse);
                    return new ResponseEntity<>(data, HttpStatus.OK);
                })
                .exceptionally(ex -> {
                    System.err.println("Error during request processing: " + ex.getMessage());
                    ex.printStackTrace();
                    ArrayList<OrderGetClientOrdersResponse> errorResponse = new ArrayList<>();
                    Map<String, ArrayList<OrderGetClientOrdersResponse>> emptyResponse = new HashMap<>();
                    emptyResponse.put("orders", errorResponse);
                    return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
                });
    }




}

