package com.example.homiyummy.controller;

import com.example.homiyummy.model.order.OrderCreatedResponse;
import com.example.homiyummy.model.order.OrderDTO;
import com.example.homiyummy.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;



    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<OrderCreatedResponse>> create(@RequestBody OrderDTO orderDTO) {
        return orderService.findLastId(orderDTO.getUid())
                .thenCompose(lastId -> orderService.createOrder(orderDTO, lastId))
                .thenApply(orderCreatedResponse -> new ResponseEntity<>(orderCreatedResponse, HttpStatus.OK))
                .exceptionally(ex -> {
                    // Handle exception and return appropriate response
                    throw new RuntimeException(ex);
                });
    }
}
