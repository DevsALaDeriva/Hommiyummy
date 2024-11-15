package com.example.homiyummy.controller;

import com.example.homiyummy.model.restaurant.RestaurantDTO;
import com.example.homiyummy.model.restaurant.RestaurantReadRequest;
import com.example.homiyummy.model.restaurant.RestaurantReadResponse;
import com.example.homiyummy.model.restaurant.RestaurantResponse;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    private final AuthService authService;

    private final RestaurantService restaurantService;
    public RestaurantController(
            AuthService authService,
            RestaurantService restaurantService) {
        this.authService = authService;
        this.restaurantService = restaurantService;
    }

    // ----------------------------------------------------------------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<String> registerRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
            try {
                    String uid = authService.createUser(restaurantDTO.getEmail(), restaurantDTO.getPassword()); // REGISTRO EN AUTHENTICATION
                    restaurantDTO.setUid(uid);                                                                  // AÑADO EL UID RECIÉN CREADO AL USERDTO

                    RestaurantResponse restaurantResponse = restaurantService.createRestaurant(restaurantDTO);  // PONGO EN MARCHA EL REGISTRO EN REALTIME
                                                                                                                // COMO createRestaurant EN EL SERVICIO DEVUELVE UN UserResponse ENTREGADO POR UN FUTURO, LA OPERACIÓN ES ASÍNCRONA Y NO DA ERROR AQUÍ
                    return ResponseEntity.ok("{\"uid\": \"" + restaurantResponse.getUid() + "\"}");       // METEMOS EL UID QUE TRAE EL RestaurantResponse DESDE REALTIME EN FORMATO JSON
            }
            catch (FirebaseAuthException e) {
                System.out.println("Error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": false }");       // DEVOLVEMOS false AL FRONTEND SI HAY UN ERROR
            }
    }
    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<Map<String, Boolean>> updateRestaurant(@RequestBody RestaurantDTO restaurantDTO) {

        Boolean change = restaurantService.updateRestaurant(restaurantDTO);
        Map<String, Boolean> response = new HashMap<>();
        response.put("change", change);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/getByUID")
    public RestaurantReadResponse getRestaurant(@RequestBody RestaurantReadRequest request){
        String uid = request.getUid();
        System.out.println("--------------- uid       "+uid);
        return restaurantService.findByUid(uid);
    }

}

