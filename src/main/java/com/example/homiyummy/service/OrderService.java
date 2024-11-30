package com.example.homiyummy.service;

import com.example.homiyummy.model.order.*;
import com.example.homiyummy.model.restaurant.RestaurantGetByOrderNumberEntity;
import com.example.homiyummy.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }


    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<Integer> findLastId(String uid){

        CompletableFuture<Integer> futureId = new CompletableFuture<Integer>();

        orderRepository.findId(uid, new OrderRepository.OnOrderIdGot() {
            @Override
            public void onFindingSuccess(int actualId) {futureId.complete(actualId);}

            @Override
            public void onFindingFailure(Exception exception) {
                futureId.completeExceptionally(exception);
            }
        });

        return futureId;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<OrderCreatedResponse> createOrder(OrderDTO orderDTO, int lastOrderId){

        int newOrderId = lastOrderId + 1;
        CompletableFuture<OrderCreatedResponse> futureOrder = new CompletableFuture<>();

        orderRepository.save(orderDTO, newOrderId, new OrderRepository.OnSavingOrderCallback() {
            @Override
            public void onSavingOrderSuccess(OrderCreatedResponse orderCreatedResponse) {
                futureOrder.complete(orderCreatedResponse);
            }

            @Override
            public void onSavingOrderFailure(Exception exception) {
                futureOrder.completeExceptionally(exception);
            }
        });
        return futureOrder;
    }

    // ----------------------------------------------------------------------------------------------------------------

    public CompletableFuture<OrderWithRestaurantDataEntity> getRestaurantData (String uid){

        CompletableFuture<OrderWithRestaurantDataEntity> futureRestData = new CompletableFuture<>();

        orderRepository.getOrderDataInRestaurantSide(uid, new OrderRepository.OnOrderGotCallback() {
            @Override
            public void onFindingSuccess(OrderWithRestaurantDataEntity orderWithRestaurantDataEntity) {
                futureRestData.complete(orderWithRestaurantDataEntity);
            }

            @Override
            public void onFindingFailure(Exception exception) {
                futureRestData.completeExceptionally(exception);
            }
        });
        return futureRestData;
    }

    // ----------------------------------------------------------------------------------------------------------------



}
