package com.example.homiyummy.controller;

import com.example.homiyummy.model.dish.DishAllResponse;
import com.example.homiyummy.model.menu.MenuByPeriodRequest;
import com.example.homiyummy.model.menu.MenuResponseByPeriod;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.service.AuthService;
import com.example.homiyummy.service.DishService;
import com.example.homiyummy.service.MenuService;
import com.example.homiyummy.service.RestaurantService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    private final AuthService authService;
    private final DishService dishService;
    private final MenuService menuService;

    private final RestaurantService restaurantService;
    public RestaurantController(
            AuthService authService,
            DishService dishService,
            MenuService menuService, RestaurantService restaurantService) {
        this.authService = authService;
        this.dishService = dishService;
        this.menuService = menuService;
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
        Map<String, Boolean> response = new HashMap<>();

        String uid = restaurantDTO.getUid();

        if (uid == null || uid.isEmpty()) {
            //System.out.println("--------------------2");
            response.put("change", false);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // SI NO HAY UID DEVUELE UN  404
        }

        Boolean change = restaurantService.updateRestaurant(restaurantDTO);

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
    public CompletableFuture<ResponseEntity<DishAllResponse>> getAll(@RequestBody UserReadRequest userReadRequest) {

        String uid = userReadRequest.getUid(); // UID DEL RESTAURANTE

        if (!uid.isEmpty()) {
            return dishService.getAll(uid).thenApply(dishAllResponse ->
                    new ResponseEntity<>(dishAllResponse, HttpStatus.OK));
        }
        else {
            DishAllResponse dishAllResponse = new DishAllResponse();
            dishAllResponse.setDishes(new ArrayList<>());
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(dishAllResponse, HttpStatus.NOT_FOUND));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    // OBTIENE TODOS LOS RESTAURANTES QUE TIENEN 7 O MÁS MENÚS
    @PostMapping("/getTypeFood")
    public CompletableFuture<ResponseEntity<Map<String, Set<String>>>> getAllTypes() {
        return restaurantService.getFoodTypes()
                .thenApply(types -> new ResponseEntity<>(types, HttpStatus.OK))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    // TODO PERSONALIZAR ERROR ------------------------XXXXXXXX
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    // OBTIENE TODOS LOS RESTAURANTES
    @PostMapping("/getAll")
    public CompletableFuture<Map<String, ArrayList<RestaurantGetAllFormatResponse>>> getALL() {
        return restaurantService.getAllRestaurants()
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new HashMap<>(); // Retorna un mapa vacío en caso de error
                    // TODO PERSONALIZAR ERROR ------------------------XXXXXXXX
                });
    }


    @PostMapping("/getMenuByPeriod")
    public ResponseEntity<List<MenuResponseByPeriod>> getMenuByPeriod(@RequestBody MenuByPeriodRequest menuByPeriodRequest) {
        String uid = menuByPeriodRequest.getUid();
        int startDate = menuByPeriodRequest.getStart_date();
        int endDate = menuByPeriodRequest.getEnd_date();

        if (uid.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<MenuResponseByPeriod> menus = menuService.getMenusByDateRange(uid, startDate, endDate);
            return new ResponseEntity<>(menus, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
                    // TODO PERSONALIZAR ERROR ------------------------XXXXXXXX
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    // OBTIENE TODOS LOS RESTAURANTES QUE TIENEN 7 O MÁS MENÚS
    @PostMapping("/featuredAll")
    public CompletableFuture<ResponseEntity<ArrayList<RestaurantWithSevenDaysMenusResponse>>> getALLFeaturedRestaurant() {
        return restaurantService.getRestaurantsWithNextSevenDaysMenus()
                .thenApply(restaurants -> new ResponseEntity<>(restaurants, HttpStatus.OK))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    // TODO PERSONALIZAR ERROR ------------------------XXXXXXXX
                });
    }

    // ----------------------------------------------------------------------------------------------------------------


}

