package com.example.homiyummy.controller;

import com.example.homiyummy.model.menu.MenuInGetTasksResponse;
import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.restaurant.RestaurantReadRequest;
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
        System.out.println("Num Order en el controller: " + orderNum);
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

    @PostMapping("getClientOrders")
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

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("getRestaurantOrders")
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

    @PostMapping("getRestaurantDayWork")
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

}
