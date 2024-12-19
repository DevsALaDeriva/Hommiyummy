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
                // SI EL EMAIL ESTÁ VACÍO O ES NULO
                if(restaurantDTO.getEmail() == null || restaurantDTO.getEmail().isEmpty()){
                    // MANDAMOS UNA RESPUESTA VACÍA
                    return ResponseEntity.badRequest().body("{\"uid\": \"\"}");
                }
                // SI NO LO ESTÁ, LO MANDAMOS AL SERVICE Y GUARDAMOS EL UID EUN UNA VARIABLE
                    String uid = authService.createUser(restaurantDTO.getEmail(), restaurantDTO.getPassword());
                    // ASIGNAMOS ESE UID AL OBJETO QUE LLEGÓ
                    restaurantDTO.setUid(uid);

                    // MANDAMOS EL OBJETO, AHORA COMPLETO, AL SERVICE
                    RestaurantResponse restaurantResponse = restaurantService.createRestaurant(restaurantDTO);

                    // MANDAMOS RESPUESTA CON EL UID
                    return ResponseEntity.ok("{\"uid\": \"" + restaurantResponse.getUid() + "\"}");
            }
            catch (FirebaseAuthException e) {
                // SI HAY UN ERROR MANDAMOS UNA RESPUESTA VACÍA
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"uid\": \"\" }");
            }
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<Map<String, Boolean>> updateRestaurant(@RequestBody RestaurantDTO restaurantDTO) {

        // CREAMOS MAP EN EL QUE ENVIAREMOS LA RESPUESTA AL FRONT
        Map<String, Boolean> response = new HashMap<>();

        // EXTRAEMOS EL UID DEL USUARIO ENTRANTE
        String uid = restaurantDTO.getUid();

        // SI NO ES VÁLIDO
        if (uid == null || uid.isEmpty()) {
            // GUARDAMOS false EN EL MAP
            response.put("change", false);
            // Y LO ENVIAMOS AL FRONT
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // SI ES VÁLIDO LO MANDAMOS AL SERVICE
        Boolean change = restaurantService.updateRestaurant(restaurantDTO);

        // ASIGNAMOS AL MAP LA RESPUESTA OBTENIDA
        response.put("change", change);

        // LO ENVIAMOS AL FRONT
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------------------------------------------------------

    // ENDP0INT QUE DEVUELVE LOS DATOS DE UN RESTAURANTE PASÁNDOLE EL UID
    @PostMapping("/getByUID")
    public RestaurantReadResponse getRestaurant(@RequestBody RestaurantReadRequest request){
        // EXTRAEMOS EL UID DEL OBJETO ENTRANTE
        String uid = request.getUid();

        // LO MANDAMOS AL SERVICE (QUE NOS DEVOLVERÁ EL OBJETO EN FORMATO RestaurantReadResponse) Y SE LO ENVIAMOS AL FRONT
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

        // EXTRAEMOS EL UID DEL JSON ENTRANTE
        String uid = userReadRequest.getUid();

        // SI NO ESTÁ VACÍO
        if (!uid.isEmpty()) {
            // LO MANDAMOS AL SERVICE
            return dishService.getAll(uid)
                    // SI SALE BIEN DEVOLVEMOS AL FRONT UN OBJETO DishAllResponse DENTRO DE UN JSON
                    .thenApply(dishAllResponse ->
                    new ResponseEntity<>(dishAllResponse, HttpStatus.OK));
        }
        else {
            // SI EL UID ESTÁ VACÍO MANDAMOS UNA RESPUESTA VACÍA EN UN JSON
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

    // ENDPOINT QUE DEVUELVE LOS MENÚS EN  UN DETERMINADO PERÍODO DE TIEMPO
    @PostMapping("/getMenuByPeriod")
    public ResponseEntity<List<MenuSimpleResponse>> getMenuByPeriod(@RequestBody MenuByPeriodRequest menuByPeriodRequest) {
        // EXTRAEMOS EL UID DEL RESTAURANTE DEL JSON ENTRANTE
        String uid = menuByPeriodRequest.getUid();
        // EXTRAEMOS FECHA DE INICIO
        int startDate = menuByPeriodRequest.getStart_date();
        // EXTRAEMOS FECHA FIN
        int endDate = menuByPeriodRequest.getEnd_date();

        // SI EL UID ESTÁ VACÍO DEVOLVEMOS UNA RESPUESTA VACÍA
        if (uid.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        // SI EL UID EXISTE
        try {
            // LANZAMOS PETICIÓN AL SERVICE PARA QUE NOS DE LOS MENÚS
            List<MenuSimpleResponse> menus = menuService.getSimpleMenusByDateRange(uid, startDate, endDate);
            // MANDAMOS RESPUESTA AL FRONT CON LOS MENÚS
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
        // PEDIMOS AL SERVICE QUE OBTENGA EL RESTAURANTE PASÁNDOLE LA URL Y LO DEVOLVEMOS
        return restaurantService.getRestaurantByUrl(request.getUrl())
                .exceptionally(ex -> { // SI SALE MAL
                    ex.printStackTrace();
                    // DEVOLVEMOS  UN OBJETO VACÍO
                    return new RestaurantGetByUrlResponse();
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *
     * @param request JSON QUE CONTIENE UNICAMENTE EL UID DEL RESTAURANTE
     * @return DEVUELVE TODOS LOS PEDIDOS DE ESE RESTAURANTE EN FORMATO Map<String, ArrayList<OrderGetRestaurantOrdersResponse>
     */

    // OBTIENE TODOS LOS PEDIDOS DE UN RESTAURANTE
    @PostMapping("getOrders")
    public CompletableFuture<ResponseEntity<Map<String, ArrayList<OrderGetRestaurantOrdersResponse>>>> getRestaurantOrders(@RequestBody RestaurantReadRequest request){
        // LANZAMOS PETICIÓN AL SERVICE APORTÁNDOLE EL UID DEL RESTAURANTE
        return orderService.getRestOrders(request.getUid())
                .thenApply(ordersResponse ->{ // SI SALE MAL
                    // LLENAMOS UN MAP CON LOS PEDIDOS ENTRANTES EN FORMATO OrderResponse
                    Map<String, ArrayList<OrderGetRestaurantOrdersResponse>> response = new HashMap<>();
                    response.put("orders", ordersResponse);
                    // Y LO ENVIAMOS
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }).exceptionally(ex -> {// SI SALE MAL
                    ex.printStackTrace();
                    // CREAMOS UN MAP Y LE AÑADIMOS COMO VALOR UN ARRAY VACÍO
                    Map<String, ArrayList<OrderGetRestaurantOrdersResponse>> emptyResponse = new HashMap<>();
                    ArrayList<OrderGetRestaurantOrdersResponse> errorResponse = new ArrayList<>();
                    emptyResponse.put("orders", errorResponse);
                    // Y LO ENVIAMOS
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
        // MANDAMOS AL SERVICE LA PETICIÓN
        return orderService.getTasks(request)
                .thenApply(tasks -> { // SI SALE BIEN
                    // CREAMOS  UN MAP
                    Map<String,ArrayList<OrderGetTasksResponse>> mapResponse = new HashMap<>();
                    // LE PONEMOS COMO VALOR EL RESULTADO
                    mapResponse.put("menus", tasks);
                    // LO ENVIAMOS AL FRONT
                    return new ResponseEntity<>(mapResponse, HttpStatus.OK);
                }).exceptionally(ex -> { // SI SALE MAL
                    ex.printStackTrace();
                    // CREAMOS UN ARRAY
                    ArrayList<OrderGetTasksResponse> emptyArray = new ArrayList<>();
                    // CREAMOS UN MAP
                    Map<String, ArrayList<OrderGetTasksResponse>> mapEmpty = new HashMap<>();
                    // METEMOS EL ARRAY VACÍO COMO VALOR DEL MAP PPARA LA KEY "menus"
                    mapEmpty.put("menus", emptyArray);
                    // LO ENVIAMOS AL FRONT
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

