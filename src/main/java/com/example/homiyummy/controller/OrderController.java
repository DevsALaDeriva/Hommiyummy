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

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<OrderCreatedResponse>> create(@RequestBody OrderDTO orderDTO) {

        return restaurantService.existsByUid(orderDTO.getUid())
                .thenCompose(exists -> {
                    if(exists){
                        return orderService.findLastId(orderDTO.getUid())
                                .thenCompose(lastId -> orderService.createOrder(orderDTO, lastId))
                                .thenApply(orderCreatedResponse -> new ResponseEntity<>(orderCreatedResponse, HttpStatus.OK))
                                .exceptionally(excepcion -> {
                                    excepcion.printStackTrace();
                                    OrderCreatedResponse errorResponse = new OrderCreatedResponse(); // DEVUELVE 0 SI HAY UN ERROR
                                    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
                                });
                    }
                    else{
                        return CompletableFuture.completedFuture(                                           // -----X----X----> REVISAR ---X---X---
                                new ResponseEntity<>(new OrderCreatedResponse(), HttpStatus.NOT_FOUND));
                    }
                })
                .exceptionally(excepcion -> {
                    excepcion.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)                          // -----X----X----> REVISAR ---X---X---
                            .body(new OrderCreatedResponse());
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/getByNumOrder")
    public CompletableFuture<ResponseEntity<OrderGotByNumResponse>> getByNumOrder(@RequestBody OrderGetByNumRequest orderGetByNumRequest){
        String orderNum = orderGetByNumRequest.getNum_order();
        return orderService.getRestaurantData(orderNum)
                .thenApply(orderWithData ->
                        new ResponseEntity<>(orderWithData, HttpStatus.OK))
                .exceptionally(ex -> {
                    System.err.println("Error during request processing: " + ex.getMessage());
                    ex.printStackTrace();
                    OrderGotByNumResponse errorResponse = new OrderGotByNumResponse();
                    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("updateMenuStatus")
    public CompletableFuture<ResponseEntity<OrderUpdateStatusResponse>> updateMenuStatus(@RequestBody OrderUpdateStatusRequest request){
            return orderService.updateMenu(request)
                    .thenApply(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        OrderUpdateStatusResponse res = new OrderUpdateStatusResponse();
                        res.setChange(false);
                        return new ResponseEntity<>(res, HttpStatus.OK);
                    });
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("review/create")
    public CompletableFuture<ResponseEntity<ReviewResponse>> createReview(@RequestBody ReviewRequest request) {
        return orderService.createReviewForOrder(request)
                .thenApply(response -> ResponseEntity.ok(response)) // Respuesta 200 OK con la ReviewResponse
                .exceptionally(ex -> ResponseEntity.badRequest()
                        .body(new ReviewResponse(false))); // En caso de error, respuesta 400 con success = false
    }
    
}
