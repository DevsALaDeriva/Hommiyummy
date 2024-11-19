package com.example.homiyummy.controller;

import com.example.homiyummy.model.dish.DishAllResponse;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.DishService;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    private final AuthService authService;
    private final DishService dishService;

    private final RestaurantService restaurantService;
    public RestaurantController(
            AuthService authService,
            DishService dishService,
            RestaurantService restaurantService) {
        this.authService = authService;
        this.dishService = dishService;
        this.restaurantService = restaurantService;
    }

    // ----------------------------------------------------------------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<String> registerRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
            try {

                if(restaurantDTO.getEmail() == null || restaurantDTO.getEmail().isEmpty()){
                    //System.out.println("Error:-------------x------------ ");
                    return ResponseEntity.badRequest().body("{\"uid\": \"\"}");
                }
                    String uid = authService.createUser(restaurantDTO.getEmail(), restaurantDTO.getPassword()); // REGISTRO EN AUTHENTICATION
                    restaurantDTO.setUid(uid);                                                                  // AÑADO EL UID RECIÉN CREADO AL USERDTO

                    RestaurantResponse restaurantResponse = restaurantService.createRestaurant(restaurantDTO);  // PONGO EN MARCHA EL REGISTRO EN REALTIME
                                                                                                                // COMO createRestaurant EN EL SERVICIO DEVUELVE UN UserResponse ENTREGADO POR UN FUTURO, LA OPERACIÓN ES ASÍNCRONA Y NO DA ERROR AQUÍ
                    return ResponseEntity.ok("{\"uid\": \"" + restaurantResponse.getUid() + "\"}");       // METEMOS EL UID QUE TRAE EL RestaurantResponse DESDE REALTIME EN FORMATO JSON
            }
            catch (FirebaseAuthException e) {
                //System.out.println("Error: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": \"\" }");       // DEVOLVEMOS false AL FRONTEND SI HAY UN ERROR
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
        //System.out.println("--------------- uid       "+uid);
        return restaurantService.findByUid(uid);
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/getAllDishes")
    public CompletableFuture<ResponseEntity<DishAllResponse>> getAll(@RequestBody UserReadRequest userReadRequest) { //-----------------------

        String uid = userReadRequest.getUid(); // UID DEL RESTAURANTE

        if (!uid.isEmpty()) {
            //System.out.println("NEW DISH ID -11--------------> ");
            return dishService.getAll(uid).thenApply(dishAllResponse ->
                    new ResponseEntity<>(dishAllResponse, HttpStatus.OK));
        }
        else {
            DishAllResponse dishAllResponse = new DishAllResponse();
            dishAllResponse.setDishes(new ArrayList<>());
            //System.out.println("NEW DISH ID -22--------------> ");
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(dishAllResponse, HttpStatus.NOT_FOUND));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // OBTINE EL RESTAURANTE DESTACADO ENTRE LOS QUE TIENEN 7 O MÁS MENÚS
    @PostMapping("/featured")
    public CompletableFuture<ResponseEntity<FeaturedRestaurantResponse>> getOneFeaturedRestaurant() {
        return restaurantService.getTheOneFeaturedRestaurant()
                .thenApply(chosenRestaurant -> new ResponseEntity<>(chosenRestaurant, HttpStatus.OK))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    // TODO PERSONALIZAR ERROR
                });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // OBTIENE TODOS LOS RESTAURANTES QUE TIENEN 7 O MÁS MENÚS
    @PostMapping("/featuredAll")
    public CompletableFuture<ResponseEntity<ArrayList<RestaurantResponse>>> getALLFeaturedRestaurant() {
        return restaurantService.getAllFeaturedRestaurants()
                .thenApply(chosenRestaurant -> new ResponseEntity<>(chosenRestaurant, HttpStatus.OK))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    // TODO PERSONALIZAR ERROR
                });
    }

    // ----------------------------------------------------------------------------------------------------------------




}

