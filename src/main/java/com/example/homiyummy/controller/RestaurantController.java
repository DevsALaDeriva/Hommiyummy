package com.example.homiyummy.controller;

import com.example.homiyummy.model.dish.DishAllResponse;
import com.example.homiyummy.model.dish.MenuSimpleResponse;
import com.example.homiyummy.model.menu.MenuByPeriodRequest;
import com.example.homiyummy.model.order.OrderGetRestaurantOrdersResponse;
import com.example.homiyummy.model.order.OrderGetTasksRequest;
import com.example.homiyummy.model.order.OrderGetTasksResponse;
import com.example.homiyummy.model.restaurant.*;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.service.*;
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
    private final OrderService orderService;

    private final RestaurantService restaurantService;
    public RestaurantController(
            AuthService authService,
            DishService dishService,
            MenuService menuService,
            RestaurantService restaurantService,
            OrderService orderService) {
        this.authService = authService;
        this.dishService = dishService;
        this.menuService = menuService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *  RECOGE UNA PETICIÓN DEL FRONTEND PARA GUARDAR UN RESTAURANTE EN BBDD
     * @param restaurantDTO OBJETO RestaurantDTO EN QUE GUARDAMOS EL JSON ENTRANTE
     * @return  SI SALE BIEN -> DEVUELVE UN STRING (CON FORMATO JSON) CON EL UID ASIGNADO AL RESTAURANTE CREADO
     *          SI SALE MAL  -> DEVOLVEMOS ESE MISMO STRING CON EL UID VACÍO
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
            try {
                if(restaurantDTO.getEmail() == null || restaurantDTO.getEmail().isEmpty()){
                    return ResponseEntity.badRequest().body("{\"uid\": \"\"}");
                }
                    String uid = authService.createUser(restaurantDTO.getEmail(), restaurantDTO.getPassword());
                    restaurantDTO.setUid(uid);

                    RestaurantResponse restaurantResponse = restaurantService.createRestaurant(restaurantDTO);

                    return ResponseEntity.ok("{\"uid\": \"" + restaurantResponse.getUid() + "\"}");
            }
            catch (FirebaseAuthException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": \"\" }");
            }
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<Map<String, Boolean>> updateRestaurant(@RequestBody RestaurantDTO restaurantDTO) {
        Map<String, Boolean> response = new HashMap<>();

        String uid = restaurantDTO.getUid();

        if (uid == null || uid.isEmpty()) {
            response.put("change", false);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Boolean change = restaurantService.updateRestaurant(restaurantDTO);

        response.put("change", change);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/getByUID")
    public RestaurantReadResponse getRestaurant(@RequestBody RestaurantReadRequest request){
        String uid = request.getUid();
        return restaurantService.findByUid(uid);
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param userReadRequest CONTIENE EL UID DE UN RESTAURANTE
     * @return DEVUELVE TODOS LOS PLATOS DEL RESTAURANTE (FORMATO DishAllResponse)
     */
    @PostMapping("/getAllDishes")
    public CompletableFuture<ResponseEntity<DishAllResponse>> getAll(@RequestBody UserReadRequest userReadRequest) {

        String uid = userReadRequest.getUid();

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

    /**
     *
     * @return DEVUELVE TODOS LOS RESTAURANTES QUE TIENEN MENÚS GRABADOS, EN FORMATO RestaurantWithMenusResponse, EN UN JSON
     */
    @PostMapping("/getAll")
    public CompletableFuture<ResponseEntity<ArrayList<RestaurantWithMenusResponse>>> getRestaurantsWithMenus() {
        return restaurantService.getAllRestaurantWithMenus()
                .thenApply(restaurants -> new ResponseEntity<>(restaurants, HttpStatus.OK))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/getMenuByPeriod")
    public ResponseEntity<List<MenuSimpleResponse>> getMenuByPeriod(@RequestBody MenuByPeriodRequest menuByPeriodRequest) {
        String uid = menuByPeriodRequest.getUid();
        int startDate = menuByPeriodRequest.getStart_date();
        int endDate = menuByPeriodRequest.getEnd_date();

        if (uid.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            List<MenuSimpleResponse> menus = menuService.getSimpleMenusByDateRange(uid, startDate, endDate);
            return new ResponseEntity<>(menus, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param request CONTIENE LA URL DEL RESTAURANTE A BUSCAR
     * @return DEVUELVE TODOS LOS DATOS DEL RESTAURANTE EN FORMATO RestaurantGetByUrlResponse
     */

    @PostMapping("/getByURL")
    public CompletableFuture<RestaurantGetByUrlResponse> getByUrl(@RequestBody RestaurantGetByUrlRequest request) {
        return restaurantService.getRestaurantByUrl(request.getUrl())
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return new RestaurantGetByUrlResponse();
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param request JSON QUE CONTIENE UNICAMENTE EL UID DEL RESTAURANTE
     * @return DEVUELVE TODOS LOS PEDIDOS DE ESE RESTAURANTE
     */

    @PostMapping("getOrders")
    public CompletableFuture<ResponseEntity<Map<String, ArrayList<OrderGetRestaurantOrdersResponse>>>> getRestaurantOrders(@RequestBody RestaurantReadRequest request){
        return orderService.getRestOrders(request.getUid())
                .thenApply(ordersResponse ->{
                    Map<String, ArrayList<OrderGetRestaurantOrdersResponse>> response = new HashMap<>();
                    response.put("orders", ordersResponse);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    Map<String, ArrayList<OrderGetRestaurantOrdersResponse>> emptyResponse = new HashMap<>();
                    ArrayList<OrderGetRestaurantOrdersResponse> errorResponse = new ArrayList<>();
                    emptyResponse.put("orders", errorResponse);
                    return new ResponseEntity<>(emptyResponse, HttpStatus.OK);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param request JSON CON TRES PARÁMETROS
     *                - UID DEL RESTAURANTE
     *                - FECHA DE INICIO
     *                - FECHA DE FIN
     *
     * @return DEVUELVE TODOS LOS PEDIDOS QUE HAY (FORMATO OrderGetTasksResponse) PARA LA FRANJA DE FECHAS BUSCADA
     */

    @PostMapping("getDayWork")
    public CompletableFuture<ResponseEntity<Map<String,ArrayList<OrderGetTasksResponse>>>> getTasks(@RequestBody OrderGetTasksRequest request){
        //System.out.println(request.getUid() + " - " + request.getStart_date() + " - " + request.getEnd_date());
        return orderService.getTasks(request)
                .thenApply(tasks -> {
                    Map<String,ArrayList<OrderGetTasksResponse>> mapResponse = new HashMap<>();
                    mapResponse.put("menus", tasks);
                    return new ResponseEntity<>(mapResponse, HttpStatus.OK);
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    ArrayList<OrderGetTasksResponse> emptyArray = new ArrayList<>();
                    Map<String, ArrayList<OrderGetTasksResponse>> mapEmpty = new HashMap<>();
                    mapEmpty.put("menus", emptyArray);
                    return new ResponseEntity<>(mapEmpty, HttpStatus.OK);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param request JSON CON TRES PARÁMETROS
     *                - UID DEL RESTAURANTE
     *                - FECHA DE INICIO
     *                - FECHA DE FIN
     * @return DEVUELVE (EN FORMATO RestaurantGetAllMenusResponse) TODOS LOS MENUS DEL RESTAURANTE CON EL UID APORTADO
     */

    @PostMapping("getAllMenus")
    public CompletableFuture<RestaurantGetAllMenusResponse> getAllMenus(@RequestBody RestaurantReadRequest request){
        if(!request.getUid().isEmpty()){
            return restaurantService.getAllMenus(request.getUid())
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        RestaurantGetAllMenusResponse emptyResponse = new RestaurantGetAllMenusResponse();
                        return emptyResponse;
                    });
        }
        else{
            RestaurantGetAllMenusResponse emptyResponse = new RestaurantGetAllMenusResponse();
            return CompletableFuture.completedFuture(emptyResponse);
        }

    }

    // ----------------------------------------------------------------------------------------------------------------


}

