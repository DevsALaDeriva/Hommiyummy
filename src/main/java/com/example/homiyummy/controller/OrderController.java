package com.example.homiyummy.controller;

import com.example.homiyummy.model.menu.MenuInGetTasksResponse;
import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.restaurant.RestaurantReadRequest;
import com.example.homiyummy.model.reviews.ReviewRequest;
import com.example.homiyummy.model.reviews.ReviewResponse;
import com.example.homiyummy.model.user.UserReadRequest;
import com.example.homiyummy.repository.OrderRepository;
import com.example.homiyummy.service.OrderService;
import com.example.homiyummy.service.RestaurantService;
import com.example.homiyummy.service.UserService;
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
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, UserService userService, RestaurantService restaurantService, OrderRepository orderRepository){
        this.orderService = orderService;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.orderRepository = orderRepository;
    }

    // ----------------------------------------------------------------------------------------------------------------

    // ENDPOINT PARA CREAR UN PEDIDO
    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<OrderCreatedResponse>> create(@RequestBody OrderDTO orderDTO) {

        // RECIBIMOS UN OBJETO OrderDTO

        // VERIFICAMOS SI EL RESTAURANTE EXISTE A PARTIR DE SU UID.
        return restaurantService.existsByUid(orderDTO.getUid())
                // SI EXISTE, CONTINUAMOS
                .thenCompose(exists -> {
                    if(exists){
                        // BUSCAMOS EL ÚLTIMO ID DE PEDIDO DEL RESTAURANTE PARA ASIGNAR UNO NUEVO.
                        return orderService.findLastId(orderDTO.getUid())
                                .thenCompose(lastId ->
                                        // CREAMOS EL PEDIDO USANDO EL OrderDTO Y EL ÚLTIMO ID ENCONTRADO.
                                        orderService.createOrder(orderDTO, lastId))
                                .thenApply(orderCreatedResponse ->
                                        // SI VA BIEN, ENVIAMOS RESPUESTA CON OBJETO ESPERADO
                                        new ResponseEntity<>(orderCreatedResponse, HttpStatus.OK))
                                .exceptionally(excepcion -> {
                                    // SI HAY UN ERROR ENVIAMOS UN OBJETO VACÍO AL FRONT
                                    excepcion.printStackTrace();
                                    OrderCreatedResponse errorResponse = new OrderCreatedResponse();
                                    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
                                });
                    }
                    else{
                        // SI EL RESTAURANTE NO EXISTE MANDAMOS UN OBJETO VACÍO AL FRONT
                        return CompletableFuture.completedFuture(
                                new ResponseEntity<>(new OrderCreatedResponse(), HttpStatus.NOT_FOUND));
                    }
                })
                .exceptionally(excepcion -> {
                    // SI HAY UN ERROR AL VER SI EXISTE MANDAMOS UN OBJETO VACÍO AL FRONT
                    excepcion.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new OrderCreatedResponse());
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    // ENDPOINT PARA OBTENER UN PEDIDO POR SU NÚMERO
    @PostMapping("/getByNumOrder")
    public CompletableFuture<ResponseEntity<OrderGotByNumResponse>> getByNumOrder(@RequestBody OrderGetByNumRequest orderGetByNumRequest){

        // GUARDAMOS EL NUMERO DE PERIDO
        String orderNum = orderGetByNumRequest.getNum_order();

        // OBTENEMOS LOS DATOS DEL PEDIDO USANDO EL SERVICIO ORDER.
        return orderService.getRestaurantData(orderNum)
                .thenApply(orderWithData ->
                        // DEVOLVEMOS LOS DATOS DEL PEDIDO SI SALE BIEN
                        new ResponseEntity<>(orderWithData, HttpStatus.OK))
                .exceptionally(ex -> {
                    // MANEJAMOS LOS ERRORES
                    System.err.println("Error during request processing: " + ex.getMessage());
                    ex.printStackTrace();
                    // CREAMOS UN OBJETO VACÍO
                    OrderGotByNumResponse errorResponse = new OrderGotByNumResponse();
                    errorResponse.setReview(new ArrayList<>());
                    // Y LO MANDAMOS AL FRONT
                    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    // ENDPOINT PARA ACTUALIZAR EL ESTADO DE UN MENU
    @PostMapping("updateMenuStatus")
    public CompletableFuture<ResponseEntity<OrderUpdateStatusResponse>> updateMenuStatus(@RequestBody OrderUpdateStatusRequest request){
            return orderService.updateMenu(request)
                    // SI OK: MANDAMOS LA RESPUESTA AL FRONT
                    .thenApply(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        OrderUpdateStatusResponse res = new OrderUpdateStatusResponse();
                        res.setChange(false);
                        // MANDAMOS UN OBJETO CON false AL FRONT
                        return new ResponseEntity<>(res, HttpStatus.OK);
                    });
    }

    // ----------------------------------------------------------------------------------------------------------------

    // ENDPOINT PARA CREAR LA REVIEW DE UN PEDIDO
    @PostMapping("review/create")
    public CompletableFuture<ResponseEntity<ReviewResponse>> createReview(@RequestBody ReviewRequest request) {
        // ENVIAMOS PETICIÓN AL SERVICE
        return orderService.createReviewForOrder(request)
                // SI SALE BIEN MANDAMOS RESPUESTA
                .thenApply(response ->
                        ResponseEntity.ok(response))
                .exceptionally(ex ->
                        // SI DA ERROR MANDAMOS RESPUESTA CON "FALSE" AL FRONT
                        ResponseEntity.badRequest()
                        .body(new ReviewResponse(false)));
    }
    
}
