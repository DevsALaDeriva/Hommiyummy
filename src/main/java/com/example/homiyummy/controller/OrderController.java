package com.example.homiyummy.controller;

import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.user.UserReadResponse;
import com.example.homiyummy.service.OrderService;
import com.example.homiyummy.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService){
        this.orderService = orderService;
        this.userService = userService;
    }

    // ----------------------------------------------------------------------------------------------------------------

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<OrderCreatedResponse>> create(@RequestBody OrderDTO orderDTO) {
        return orderService.findLastId(orderDTO.getUid())
                .thenCompose(lastId -> orderService.createOrder(orderDTO, lastId))
                .thenApply(orderCreatedResponse -> new ResponseEntity<>(orderCreatedResponse, HttpStatus.OK))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    OrderCreatedResponse errorResponse = new OrderCreatedResponse(0); // DEVUELVE 0 SI HAY UN ERROR
                    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------

    /**
     *  Este métod.o hace 2 peticiones a Realtime.
     *  La 1ª orderService.getRestaurantData(orderNum) -> obtiene los datos del restaurante y el UID dl cliente que hay que mandar al front (en un objeto creado solo para ello)
     *  La 2ª userService.findUserByUid ->                obtiene los datos del cliente (usando su UID recién recibido) y los une a los del restaurante  (en un objeto creado ad hoc)
     * @param orderGetByNumRequest: el Json que envía el frontend lo convertimos en un objeto de este tipo para manipularlo en el back.
     * @return Devolvemos un objeto OrderGotByNumResponse (creado ad hoc) con las propiedades del pedido que quiere el front.
     */
    @PostMapping("/getByNumOrder")
    public CompletableFuture<ResponseEntity<OrderGotByNumResponse>> getByNumOrder(@RequestBody OrderGetByNumRequest orderGetByNumRequest){
        String orderNum = orderGetByNumRequest.getNumOrder();
        return orderService.getRestaurantData(orderNum)
                .thenCompose(orderWithRestaurantData ->
                        userService.findUserByUid(orderWithRestaurantData.getCustomerUID(), orderWithRestaurantData))
                .thenApply(orderGotByNumResponse ->
                        new ResponseEntity<>(orderGotByNumResponse, HttpStatus.OK))
                .exceptionally(ex -> {
                    System.err.println("Error during request processing: " + ex.getMessage());
                    ex.printStackTrace();
                    OrderGotByNumResponse errorResponse = new OrderGotByNumResponse();
                    return new ResponseEntity<>(errorResponse, HttpStatus.OK);
                });
    }

    // ----------------------------------------------------------------------------------------------------------------



}
